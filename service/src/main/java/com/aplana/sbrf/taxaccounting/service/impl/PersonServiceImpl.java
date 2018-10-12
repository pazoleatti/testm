package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.async.AbstractStartupAsyncTaskHandler;
import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.dao.impl.IdDocDaoImpl;
import com.aplana.sbrf.taxaccounting.dao.impl.IdTaxPayerDaoImpl;
import com.aplana.sbrf.taxaccounting.dao.impl.PersonTbDaoImpl;
import com.aplana.sbrf.taxaccounting.dao.impl.components.RegistryPersonUpdateQueryBuilder;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookPersonDao;
import com.aplana.sbrf.taxaccounting.dao.util.DBUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.action.PersonOriginalAndDuplicatesAction;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.filter.refbook.RefBookPersonFilter;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;
import com.aplana.sbrf.taxaccounting.model.result.CheckDulResult;
import com.aplana.sbrf.taxaccounting.permissions.BasePermissionEvaluator;
import com.aplana.sbrf.taxaccounting.permissions.PersonVipDataPermission;
import com.aplana.sbrf.taxaccounting.script.service.util.ScriptUtils;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.LockDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.PersonService;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import com.aplana.sbrf.taxaccounting.utils.SimpleDateUtils;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Сервис работы Физическими лицами. Заменяет некоторые операции провайдера справочников для лучшей производительности
 */
@Service
@Transactional
public class PersonServiceImpl implements PersonService {

    @Autowired
    private RefBookPersonDao refBookPersonDao;
    @Autowired
    private CommonRefBookService commonRefBookService;
    @Autowired
    private LogEntryService logEntryService;
    @Autowired
    private RefBookDao refBookDao;
    @Autowired
    private LockDataService lockDataService;
    @Autowired
    private AsyncManager asyncManager;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private BasePermissionEvaluator permissionEvaluator;
    @Autowired
    private RegistryPersonUpdateQueryBuilder registryPersonUpdateBuilder;
    @Autowired
    private IdDocDaoImpl idDocDaoImpl;
    @Autowired
    private IdTaxPayerDaoImpl idTaxPayerDaoImpl;
    @Autowired
    private PersonTbDaoImpl personTbDaoImpl;
    @Autowired
    private DBUtils dbUtils;


    private static final ThreadLocal<SimpleDateFormat> formatter = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };

    @Override
    public RegistryPersonDTO fetchOriginal(Long id, Date actualDate) {
        List<RegistryPersonDTO> versions = refBookPersonDao.fetchOriginal(id);
        return resolveVersion(versions, actualDate);
    }

    @Override
    public PagingResult<RegistryPersonDTO> fetchDuplicates(Long id, Date actualDate, PagingParams pagingParams) {
        List<RegistryPersonDTO> toReturnData = new ArrayList<>();
        List<RegistryPersonDTO> candidates = refBookPersonDao.fetchDuplicates(id, pagingParams);
        Map<Long, List<RegistryPersonDTO>> candidatesGroupedByOldId = new HashMap<>();
        for (RegistryPersonDTO candidate : candidates) {
            List<RegistryPersonDTO> group = candidatesGroupedByOldId.get(candidate.getOldId());
            if (group == null) {
                group = new ArrayList<>();
                group.add(candidate);
                candidatesGroupedByOldId.put(candidate.getOldId(), group);
            } else {
                group.add(candidate);
            }
        }
        for (List<RegistryPersonDTO> groupContent : candidatesGroupedByOldId.values()) {
            RegistryPersonDTO person = resolveVersion(groupContent, actualDate);
            boolean viewVipDataGranted = permissionEvaluator.hasPermission(SecurityContextHolder.getContext().getAuthentication(), person, PersonVipDataPermission.VIEW_VIP_DATA);
            RefBook refBookIdDoc = refBookDao.get(RefBook.Id.ID_DOC.getId());
            if (viewVipDataGranted) {
                PagingResult<Map<String, RefBookValue>> idDocs = refBookDao.getRecordsWithVersionInfo(refBookIdDoc, null, null, "frb.id = " + person.getReportDoc().value().get("REPORT_DOC"), null, "asc");
                if (!idDocs.isEmpty()) {
                    person.setReportDoc(Permissive.of(commonRefBookService.dereference(refBookIdDoc, idDocs).get(0)));
                } else {
                    person.setReportDoc(Permissive.<Map<String, RefBookValue>>of(null));
                }
            } else {
                person.setReportDoc(Permissive.<Map<String, RefBookValue>>forbidden());
                person.setInn(Permissive.<String>forbidden());
                person.setSnils(Permissive.<String>forbidden());
                person.setInnForeign(Permissive.<String>forbidden());
                person.setReportDoc(Permissive.<Map<String, RefBookValue>>forbidden());
            }
            toReturnData.add(person);
        }
        return new PagingResult<>(toReturnData, toReturnData.size());
    }

    @Override
    public PagingResult<RefBookPerson> getPersons(PagingParams pagingParams, RefBookPersonFilter filter, TAUser requestingUser) {
        PagingResult<RefBookPerson> persons = refBookPersonDao.getPersons(pagingParams, filter);
        forbidVipsDataByUserPermissions(persons, requestingUser);
        return persons;
    }

    private void forbidVipsDataByUserPermissions(List<RefBookPerson> persons, TAUser user) {
        if (user.hasRole(TARole.N_ROLE_CONTROL_UNP)) {
            return;
        }

        List<Integer> departmentsAvailableToUser = departmentService.getBADepartmentIds(user);
        if (departmentsAvailableToUser.isEmpty()) {
            forbidAllVips(persons);
        } else {
            forbidVipsWithNotAvailableTBs(persons, departmentsAvailableToUser);
        }
    }

    private void forbidVipsWithNotAvailableTBs(List<RefBookPerson> persons, List<Integer> permittedDepartments) {
        Set<Integer> permittedDepartmentsSet = new HashSet<>(permittedDepartments);
        for (RefBookPerson person : persons) {
            if (person.isVip()) {
                List<Integer> vipDepartments = refBookPersonDao.getPersonTbIds(person.getId());
                Set<Integer> vipDepartmentsSet = new HashSet<>(vipDepartments);
                Set<Integer> intersection = Sets.intersection(permittedDepartmentsSet, vipDepartmentsSet);
                if (intersection.isEmpty()) {
                    person.forbid();
                }
            }
        }
    }

    private void forbidAllVips(List<RefBookPerson> persons) {
        for (RefBookPerson person : persons) {
            if (person.isVip()) {
                person.forbid();
            }
        }
    }


    @Override
    public int getPersonsCount(RefBookPersonFilter filter) {
        return refBookPersonDao.getPersonsCount(filter);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> fetchPersonsAsMap(Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        PagingResult<Map<String, RefBookValue>> records = refBookPersonDao.fetchPersonsAsMap(version, pagingParams, filter, sortAttribute);
        return commonRefBookService.dereference(refBookPersonDao.getRefBook(), records);
    }

    @Override
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).VIEW_NSI)")
    public ActionResult saveOriginalAndDuplicates(TAUserInfo userInfo, PersonOriginalAndDuplicatesAction data) {
        ActionResult result = new ActionResult();
        if (data.getAddedOriginal() != null && data.getAddedOriginalVersionId() != null) {
            RegistryPersonDTO original = refBookPersonDao.fetchPersonWithVersionInfo(data.getAddedOriginalVersionId());
            if (!original.getOldId().equals(original.getRecordId())) {
                Logger logger = new Logger();
                logger.error("ФЛ%s%s%s (Идентификатор ФЛАГ: %s) не может быть назначен оригиналом, так как сам является дубликатом другого ФЛ.",
                        original.getLastName() != null ? " " + original.getLastName() : "",
                        original.getFirstName() != null ? " " + original.getFirstName() : "",
                        original.getMiddleName() != null ? " " + original.getMiddleName() : "",
                        original.getOldId());
                result.setUuid(logEntryService.save(logger.getEntries()));
            }
            refBookPersonDao.setOriginal(data.getChangingPersonRecordId(), data.getChangingPersonOldId(), data.getAddedOriginal());
        }
        if (data.isDeleteOriginal()) {
            refBookPersonDao.deleteOriginal(data.getChangingPersonRecordId(), data.getChangingPersonOldId());
        }
        if (CollectionUtils.isNotEmpty(data.getAddedDuplicates())) {
            refBookPersonDao.setDuplicates(data.getAddedDuplicates(), data.getChangingPersonRecordId());
        }
        if (CollectionUtils.isNotEmpty(data.getDeletedDuplicates())) {
            refBookPersonDao.deleteDuplicates(data.getDeletedDuplicates());
        }
        return result;
    }

    @Override
    public String createSearchFilter(String firstName, String lastName, String searchPattern, Boolean exactSearch) {
        String filter = "";
        // Отдельная фильтрация по имени и фамилии - выполняем сначала, чтобы меньше результатов попало под полнотекстовый поиск
        if (StringUtils.isNotEmpty(firstName)) {
            String prepared = prepareForQuery(firstName);
            filter += "LOWER(FIRST_NAME) :searchPattern".replace(":searchPattern", (exactSearch ? "= '" + prepared + "'" : "like '%" + prepared + "%'"));
        }
        if (StringUtils.isNotEmpty(lastName)) {
            String prepared = prepareForQuery(lastName);
            if (StringUtils.isNotEmpty(filter)) {
                filter += " and ";
            }
            filter += "LOWER(LAST_NAME) :searchPattern".replace(":searchPattern", (exactSearch ? "= '" + prepared + "'" : "like '%" + prepared + "%'"));
        }

        // Полнотекстовый поиск
        if (StringUtils.isNotEmpty(searchPattern)) {
            String prepared = prepareForQuery(searchPattern);
            if (StringUtils.isNotEmpty(filter)) {
                filter += " and ";
            }
            // Пытаемся распарсить дату из строки поиска, пробуем только один формат, т.к он используется в таблице - если не получилось, то игнорируем поиск по дате
            Date birthDate = null;
            try {
                birthDate = formatter.get().parse(prepared);
            } catch (ParseException ignore) {
            }
            filter += ("(TO_CHAR(RECORD_ID) :searchPattern or " +
                    (StringUtils.isEmpty(lastName) ? "LOWER(LAST_NAME) :searchPattern or " : "") +
                    (StringUtils.isEmpty(firstName) ? "LOWER(FIRST_NAME) :searchPattern or " : "") +
                    "LOWER(MIDDLE_NAME) :searchPattern or " +
                    "LOWER(INN) :searchPattern or " +
                    "LOWER(INN_FOREIGN) :searchPattern or " +
                    "LOWER(SNILS) :searchPattern or " +
                    "LOWER(TAXPAYER_STATE_CODE) :searchPattern or " +
                    "LOWER(BIRTH_PLACE) :searchPattern or " +
                    "LOWER(CITIZENSHIP_CODE) :searchPattern or " +
                    "TO_CHAR(EMPLOYEE) :searchPattern or " +
                    "LOWER(SOURCE_ID_CODE) :searchPattern or " +
                    "TO_CHAR(OLD_ID) :searchPattern or " +
                    (birthDate != null ? "to_char(BIRTH_DATE, 'DD.MM.YYYY') = '" + prepared + "' or " : "") +
                    "LOWER(ADDRESS_ADDRESS_FULL) :searchPattern)")
                    .replaceAll(":searchPattern", (exactSearch ? "= '" + prepared + "'" : "like '%" + prepared + "%'"));
        }

        return filter;
    }

    @Override
    public RegistryPersonDTO fetchPerson(Long id) {

        RegistryPersonDTO person = refBookPersonDao.fetchPersonWithVersionInfo(id);
        boolean viewVipDataGranted = permissionEvaluator.hasPermission(SecurityContextHolder.getContext().getAuthentication(), person, PersonVipDataPermission.VIEW_VIP_DATA);
        RefBook refBookIdDoc = refBookDao.get(RefBook.Id.ID_DOC.getId());
        RefBook refBookCountry = refBookDao.get(RefBook.Id.COUNTRY.getId());
        RefBook refBookTaxPayerState = refBookDao.get(RefBook.Id.TAXPAYER_STATUS.getId());
        RefBook refBookAsnu = refBookDao.get(RefBook.Id.ASNU.getId());
        RefBook refBookAddress = refBookDao.get(RefBook.Id.PERSON_ADDRESS.getId());

        if (!viewVipDataGranted) {
            person.setInn(Permissive.<String>forbidden());
            person.setInnForeign(Permissive.<String>forbidden());
            person.setSnils(Permissive.<String>forbidden());
        }

        if (viewVipDataGranted) {
            PagingResult<Map<String, RefBookValue>> refBookTaxPayerStates = refBookDao.getRecordsWithVersionInfo(refBookTaxPayerState, null, null, "frb.id = " + person.getTaxPayerState().value().get("TAXPAYER_STATE").getReferenceValue(), null, "asc");
            if (!refBookTaxPayerStates.isEmpty()) {
                person.setTaxPayerState(Permissive.of(commonRefBookService.dereference(refBookTaxPayerState, refBookTaxPayerStates).get(0)));
            } else {
                person.setTaxPayerState(Permissive.<Map<String, RefBookValue>>of(null));
            }
        } else {
            person.setTaxPayerState(Permissive.<Map<String, RefBookValue>>forbidden());
        }


        PagingResult<Map<String, RefBookValue>> citizenships = refBookDao.getRecordsWithVersionInfo(refBookCountry, null, null, "frb.id = " + person.getCitizenship().value().get("CITIZENSHIP").getReferenceValue(), null, "asc");
        if (!citizenships.isEmpty()) {
            person.setCitizenship(Permissive.of(commonRefBookService.dereference(refBookCountry, citizenships).get(0)));
        } else {
            person.setCitizenship(Permissive.<Map<String, RefBookValue>>of(null));
        }


        PagingResult<Map<String, RefBookValue>> sources = refBookDao.getRecordsWithVersionInfo(refBookAsnu, null, null, "frb.id = " + person.getSource().get("SOURCE_ID").getReferenceValue(), null, "asc");
        if (!sources.isEmpty()) {
            person.setSource(commonRefBookService.dereference(refBookAsnu, sources).get(0));
        } else {
            person.setSource(null);
        }

        if (viewVipDataGranted) {
            PagingResult<Map<String, RefBookValue>> addresses = refBookDao.getRecordsWithVersionInfo(refBookAddress, null, null, "frb.id = " + person.getAddress().value().get("ADDRESS").getReferenceValue(), null, "asc");
            if (!addresses.isEmpty()) {
                person.setAddress(Permissive.of(commonRefBookService.dereference(refBookAddress, addresses).get(0)));
            } else {
                person.setAddress(Permissive.<Map<String, RefBookValue>>of(null));
            }
        } else {
            person.setAddress(Permissive.<Map<String, RefBookValue>>forbidden());
        }

        if (viewVipDataGranted) {
            PagingResult<Map<String, RefBookValue>> idDocs = refBookDao.getRecordsWithVersionInfo(refBookIdDoc, null, null, "frb.id = " + person.getReportDoc().value().get("REPORT_DOC"), null, "asc");
            if (!idDocs.isEmpty()) {
                person.setReportDoc(Permissive.of(commonRefBookService.dereference(refBookIdDoc, idDocs).get(0)));
            } else {
                person.setReportDoc(Permissive.<Map<String, RefBookValue>>of(null));
            }
        } else {
            person.setReportDoc(Permissive.<Map<String, RefBookValue>>forbidden());
        }
        return person;
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> fetchReferencesList(Long recordId, Long refBookId, PagingParams pagingParams) {
        RefBook actualRefBook = refBookDao.get(refBookId);
        RefBook refBookPerson = refBookDao.get(RefBook.Id.PERSON.getId());
        PagingResult<Map<String, RefBookValue>> persons = refBookDao.getRecords(refBookPerson.getId(), refBookPerson.getTableName(), pagingParams, null, null, "record_id = " + recordId + " AND status = 0");
        Long[] versionIds = new Long[persons.size()];
        for (int i = 0; i < persons.size(); i++) {
            versionIds[i] = persons.get(i).get("id").getNumberValue().longValue();
        }
        PagingResult<Map<String, RefBookValue>> result = new PagingResult<>();
        if (versionIds.length > 0) {
            result = refBookDao.getRecords(refBookId, actualRefBook.getTableName(), pagingParams, null, null, "person_id in (" + StringUtils.join(versionIds, ", ") + ") AND status = 0");
        }
        return commonRefBookService.dereference(actualRefBook, result);
    }

    @Override
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).EXPORT_PERSONS)")
    public ActionResult createTaskToCreateExcel(RefBookPersonFilter filter, PagingParams pagingParams, TAUserInfo userInfo) {
        Logger logger = new Logger();
        ActionResult result = new ActionResult();
        AsyncTaskType taskType = AsyncTaskType.EXCEL_PERSONS;

        Map<String, Object> params = new HashMap<>();
        params.put("personsFilter", filter);
        params.put("pagingParams", pagingParams);

        String keyTask = "EXCEL_PERSONS_" + System.currentTimeMillis();
        asyncManager.executeTask(keyTask, taskType, userInfo, params, logger, false, new AbstractStartupAsyncTaskHandler() {
            @Override
            public LockData lockObject(String keyTask, AsyncTaskType reportType, TAUserInfo userInfo) {
                return lockDataService.lockAsync(keyTask, userInfo.getUser().getId());
            }
        });
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    @Override
    @PreAuthorize("hasPermission(#person, T(com.aplana.sbrf.taxaccounting.permissions.PersonVipDataPermission).VIEW_VIP_DATA)")
    public void updateRegistryPerson(RegistryPersonDTO person) {
        RegistryPersonDTO persistedPerson = fetchPerson(person.getId());
        List<RegistryPersonDTO.UpdatableField> personFieldsToUpdate = new ArrayList<>();
        boolean viewVipDataGranted = permissionEvaluator.hasPermission(SecurityContextHolder.getContext().getAuthentication(), person, PersonVipDataPermission.VIEW_VIP_DATA);

        person.setVersion(SimpleDateUtils.toStartOfDay(person.getVersion()));
        persistedPerson.setVersion(SimpleDateUtils.toStartOfDay(persistedPerson.getVersion()));

        if (!person.getVersion().equals(persistedPerson.getVersion()))
            personFieldsToUpdate.add(RegistryPersonDTO.UpdatableField.VERSION);
        if (!Optional.fromNullable(person.getLastName()).equals(Optional.fromNullable(persistedPerson.getLastName())))
            personFieldsToUpdate.add(RegistryPersonDTO.UpdatableField.LAST_NAME);
        if (!Optional.fromNullable(person.getFirstName()).equals(Optional.fromNullable(persistedPerson.getFirstName())))
            personFieldsToUpdate.add(RegistryPersonDTO.UpdatableField.FIRST_NAME);
        if (!Optional.fromNullable(person.getMiddleName()).equals(Optional.fromNullable(persistedPerson.getMiddleName())))
            personFieldsToUpdate.add(RegistryPersonDTO.UpdatableField.MIDDLE_NAME);
        if (!Optional.fromNullable(SimpleDateUtils.toStartOfDay(person.getBirthDate())).equals(Optional.fromNullable(SimpleDateUtils.toStartOfDay(persistedPerson.getBirthDate()))))
            personFieldsToUpdate.add(RegistryPersonDTO.UpdatableField.BIRTH_DATE);
        if (!Optional.fromNullable(person.getCitizenship()).equals(Optional.fromNullable(persistedPerson.getCitizenship())))
            personFieldsToUpdate.add(RegistryPersonDTO.UpdatableField.CITIZENSHIP);
        if (!Optional.fromNullable(person.getSource()).equals(Optional.fromNullable(persistedPerson.getSource())))
            personFieldsToUpdate.add(RegistryPersonDTO.UpdatableField.SOURCE);
        if (viewVipDataGranted) {
            if (!Optional.fromNullable(person.getReportDoc()).equals(Optional.fromNullable(persistedPerson.getReportDoc())))
                personFieldsToUpdate.add(RegistryPersonDTO.UpdatableField.REPORT_DOC);
            if (!Optional.fromNullable(person.getInn()).equals(Optional.fromNullable(persistedPerson.getInn())))
                personFieldsToUpdate.add(RegistryPersonDTO.UpdatableField.INN);
            if (!Optional.fromNullable(person.getInnForeign()).equals(Optional.fromNullable(persistedPerson.getInnForeign())))
                personFieldsToUpdate.add(RegistryPersonDTO.UpdatableField.INN_FOREIGN);
            if (!Optional.fromNullable(person.getSnils()).equals(Optional.fromNullable(persistedPerson.getSnils())))
                personFieldsToUpdate.add(RegistryPersonDTO.UpdatableField.SNILS);
            if (!Optional.fromNullable(person.getTaxPayerState()).equals(Optional.fromNullable(persistedPerson.getTaxPayerState())))
                personFieldsToUpdate.add(RegistryPersonDTO.UpdatableField.TAX_PAYER_STATE);
            if (!person.getVip() == persistedPerson.getVip())
                personFieldsToUpdate.add(RegistryPersonDTO.UpdatableField.VIP);
        }

        List<RegistryPersonDTO.UpdatableField> addressFieldsToUpdate = new ArrayList<>();
        if (viewVipDataGranted) {
            String newRegionCode = person.getAddress() != null && person.getAddress().value() != null ? person.getAddress().value().get(RegistryPersonDTO.UpdatableField.REGION_CODE.getAlias()).getStringValue() : null;
            String oldRegionCode = persistedPerson.getAddress() != null && persistedPerson.getAddress().value() != null ? persistedPerson.getAddress().value().get(RegistryPersonDTO.UpdatableField.REGION_CODE.getAlias()).getStringValue() : null;
            String newPostalCode = person.getAddress() != null && person.getAddress().value() != null ? person.getAddress().value().get(RegistryPersonDTO.UpdatableField.POSTAL_CODE.getAlias()).getStringValue() : null;
            String oldPostalCode = persistedPerson.getAddress() != null && persistedPerson.getAddress().value() != null ? persistedPerson.getAddress().value().get(RegistryPersonDTO.UpdatableField.POSTAL_CODE.getAlias()).getStringValue() : null;
            String newDistrict = person.getAddress() != null && person.getAddress().value() != null ? person.getAddress().value().get(RegistryPersonDTO.UpdatableField.DISTRICT.getAlias()).getStringValue() : null;
            String oldDistrict = persistedPerson.getAddress() != null && persistedPerson.getAddress().value() != null ? persistedPerson.getAddress().value().get(RegistryPersonDTO.UpdatableField.DISTRICT.getAlias()).getStringValue() : null;
            String newCity = person.getAddress() != null && person.getAddress().value() != null ? person.getAddress().value().get(RegistryPersonDTO.UpdatableField.CITY.getAlias()).getStringValue() : null;
            String oldCity = persistedPerson.getAddress() != null && persistedPerson.getAddress().value() != null ? persistedPerson.getAddress().value().get(RegistryPersonDTO.UpdatableField.CITY.getAlias()).getStringValue() : null;
            String newLocality = person.getAddress() != null && person.getAddress().value() != null ? person.getAddress().value().get(RegistryPersonDTO.UpdatableField.LOCALITY.getAlias()).getStringValue() : null;
            String oldLocality = persistedPerson.getAddress() != null && persistedPerson.getAddress().value() != null ? persistedPerson.getAddress().value().get(RegistryPersonDTO.UpdatableField.LOCALITY.getAlias()).getStringValue() : null;
            String newStreet = person.getAddress() != null && person.getAddress().value() != null ? person.getAddress().value().get(RegistryPersonDTO.UpdatableField.STREET.getAlias()).getStringValue() : null;
            String oldStreet = persistedPerson.getAddress() != null && persistedPerson.getAddress().value() != null ? persistedPerson.getAddress().value().get(RegistryPersonDTO.UpdatableField.STREET.getAlias()).getStringValue() : null;
            String newHouse = person.getAddress() != null && person.getAddress().value() != null ? person.getAddress().value().get(RegistryPersonDTO.UpdatableField.HOUSE.getAlias()).getStringValue() : null;
            String oldHouse = persistedPerson.getAddress() != null && persistedPerson.getAddress().value() != null ? persistedPerson.getAddress().value().get(RegistryPersonDTO.UpdatableField.HOUSE.getAlias()).getStringValue() : null;
            String newBuild = person.getAddress() != null && person.getAddress().value() != null ? person.getAddress().value().get(RegistryPersonDTO.UpdatableField.BUILD.getAlias()).getStringValue() : null;
            String oldBuild = persistedPerson.getAddress() != null && persistedPerson.getAddress().value() != null ? persistedPerson.getAddress().value().get(RegistryPersonDTO.UpdatableField.BUILD.getAlias()).getStringValue() : null;
            String newAppartment = person.getAddress() != null && person.getAddress().value() != null ? person.getAddress().value().get(RegistryPersonDTO.UpdatableField.APPARTMENT.getAlias()).getStringValue() : null;
            String oldAppartment = persistedPerson.getAddress() != null && persistedPerson.getAddress().value() != null ? persistedPerson.getAddress().value().get(RegistryPersonDTO.UpdatableField.APPARTMENT.getAlias()).getStringValue() : null;
            Long newCountryId = person.getAddress() != null && person.getAddress().value() != null ? person.getAddress().value().get(RegistryPersonDTO.UpdatableField.COUNTRY_ID.getAlias()).getReferenceValue() : null;
            Long oldCountryId = persistedPerson.getAddress() != null && persistedPerson.getAddress().value() != null && persistedPerson.getAddress().value().get(RegistryPersonDTO.UpdatableField.COUNTRY_ID.getAlias()).getValue() != null ? ((Map<String, RefBookValue>) persistedPerson.getAddress().value().get(RegistryPersonDTO.UpdatableField.COUNTRY_ID.getAlias()).getValue()).get("id").getNumberValue().longValue() : null;
            String newAddress = person.getAddress() != null && person.getAddress().value() != null ? person.getAddress().value().get(RegistryPersonDTO.UpdatableField.ADDRESS.getAlias()).getStringValue() : null;
            String oldAddress = persistedPerson.getAddress() != null && persistedPerson.getAddress().value() != null ? persistedPerson.getAddress().value().get(RegistryPersonDTO.UpdatableField.ADDRESS.getAlias()).getStringValue() : null;


            if ((newRegionCode != null && !newRegionCode.equalsIgnoreCase(oldRegionCode))
                    || (newRegionCode == null && oldRegionCode != null))
                addressFieldsToUpdate.add(RegistryPersonDTO.UpdatableField.REGION_CODE);
            if ((newPostalCode != null && !newPostalCode.equalsIgnoreCase(oldPostalCode))
                    || (newPostalCode == null && oldPostalCode != null))
                addressFieldsToUpdate.add(RegistryPersonDTO.UpdatableField.POSTAL_CODE);
            if ((newDistrict != null && !newDistrict.equalsIgnoreCase(oldDistrict))
                    || (newDistrict == null && oldDistrict != null))
                addressFieldsToUpdate.add(RegistryPersonDTO.UpdatableField.DISTRICT);
            if ((newCity != null && !newCity.equalsIgnoreCase(oldCity))
                    || (newCity == null && oldCity != null))
                addressFieldsToUpdate.add(RegistryPersonDTO.UpdatableField.CITY);
            if ((newLocality != null && !newLocality.equalsIgnoreCase(oldLocality))
                    || (newLocality == null && oldLocality != null))
                addressFieldsToUpdate.add(RegistryPersonDTO.UpdatableField.LOCALITY);
            if ((newStreet != null && !newStreet.equalsIgnoreCase(oldStreet))
                    || (newStreet == null && oldStreet != null))
                addressFieldsToUpdate.add(RegistryPersonDTO.UpdatableField.STREET);
            if ((newHouse != null && !newHouse.equalsIgnoreCase(oldHouse))
                    || (newHouse == null && oldHouse != null))
                addressFieldsToUpdate.add(RegistryPersonDTO.UpdatableField.HOUSE);
            if ((newBuild != null && !newBuild.equalsIgnoreCase(oldBuild))
                    || (newBuild == null && oldBuild != null))
                addressFieldsToUpdate.add(RegistryPersonDTO.UpdatableField.BUILD);
            if ((newAppartment != null && !newAppartment.equalsIgnoreCase(oldAppartment))
                    || (newAppartment == null && oldAppartment != null))
                addressFieldsToUpdate.add(RegistryPersonDTO.UpdatableField.APPARTMENT);
            if ((newCountryId != null && !newCountryId.equals(oldCountryId))
                    || (newCountryId == null && oldCountryId != null))
                addressFieldsToUpdate.add(RegistryPersonDTO.UpdatableField.COUNTRY_ID);
            if ((newAddress != null && !newAddress.equalsIgnoreCase(oldAddress))
                    || (newAddress == null && oldAddress != null))
                addressFieldsToUpdate.add(RegistryPersonDTO.UpdatableField.ADDRESS);
        }

        if (person.getRecordVersionTo() != null) {
            if (!SimpleDateUtils.toStartOfDay(person.getRecordVersionTo()).equals(SimpleDateUtils.toStartOfDay(persistedPerson.getRecordVersionTo()))) {
                refBookPersonDao.deleteRegistryPersonFakeVersion(person.getRecordId());
                if (SimpleDateUtils.toStartOfDay(person.getRecordVersionTo()).compareTo(SimpleDateUtils.toStartOfDay(person.getVersion())) > 0) {
                    person.setVersionEnd(SimpleDateUtils.toStartOfDay(persistedPerson.getRecordVersionTo()));
                    refBookPersonDao.saveRegistryPersonFakeVersion(person);
                }
            }
        }

        String personSql = registryPersonUpdateBuilder.buildPersonUpdateQuery(personFieldsToUpdate);
        String addressSql = registryPersonUpdateBuilder.buildAddressUpdateQuery(addressFieldsToUpdate);

        if (personSql != null) {
            refBookPersonDao.updateRegistryPerson(person, personSql);
        }

        if (addressSql != null) {
            refBookPersonDao.updateRegistryPersonAddress(person.getAddress().value(), addressSql);
        }

        if (personFieldsToUpdate.contains(RegistryPersonDTO.UpdatableField.REPORT_DOC)) {
            Long oldRepDocId = persistedPerson.getReportDoc().value() != null ? persistedPerson.getReportDoc().value().get("id").getNumberValue().longValue() : null;
            Long newRepDocId = person.getReportDoc().value() != null ? person.getReportDoc().value().get("id").getNumberValue().longValue() : null;
            refBookPersonDao.updateRegistryPersonIncRepDocId(oldRepDocId, newRepDocId);
        }
    }

    @Override
    public void checkVersionOverlapping(RegistryPersonDTO person) {
        Date minDate = null, maxDate = new Date(0);
        List<RegistryPersonDTO> overlappingPersonList = new ArrayList<>();
        RefBookPersonFilter filter = new RefBookPersonFilter();
        filter.setId(person.getRecordId().toString());
        List<RegistryPersonDTO> relatedPersons = refBookPersonDao.fetchNonDuplicatesVersions(person.getRecordId());
        for (RegistryPersonDTO relatedPerson : relatedPersons) {
            if (person.getId() == null || !person.getId().equals(relatedPerson.getId()) && person.getRecordId().equals(person.getOldId())) {
                // Проверка пересечения существующей с исходной
                if (!(SimpleDateUtils.toStartOfDay(relatedPerson.getRecordVersionTo()) != null && SimpleDateUtils.toStartOfDay(person.getVersion()).after(SimpleDateUtils.toStartOfDay(relatedPerson.getRecordVersionTo()))
                        || SimpleDateUtils.toStartOfDay(person.getVersion()).before(SimpleDateUtils.toStartOfDay(relatedPerson.getVersion()))
                        && person.getVersion() != null && SimpleDateUtils.toStartOfDay(person.getRecordVersionTo()).before(SimpleDateUtils.toStartOfDay(relatedPerson.getVersion())))) {
                    overlappingPersonList.add(relatedPerson);
                }
                if (minDate == null || SimpleDateUtils.toStartOfDay(relatedPerson.getVersion()).before(SimpleDateUtils.toStartOfDay(minDate))) {
                    minDate = relatedPerson.getVersion();
                }
                if (maxDate != null && (SimpleDateUtils.toStartOfDay(relatedPerson.getRecordVersionTo()) == null || SimpleDateUtils.toStartOfDay(relatedPerson.getRecordVersionTo()).after(SimpleDateUtils.toStartOfDay(maxDate)))) {
                    maxDate = relatedPerson.getRecordVersionTo();
                }
            }
        }
        if (!overlappingPersonList.isEmpty()) {
            throw new ServiceException("Невозможно сохранить данные физического лица. Период действия с %s по %s для версии %s ФЛ (%s)%s, %s пересекается с периодом действия других версий этого ФЛ: %s",
                    FastDateFormat.getInstance("dd.MM.yyyy").format(person.getVersion()),
                    person.getRecordVersionTo() == null ? "__" : FastDateFormat.getInstance("dd.MM.yyyy").format(person.getRecordVersionTo()),
                    person.getId(),
                    person.getRecordId(),
                    (person.getLastName() != null ? " " + person.getLastName() : "") + (person.getFirstName() != null ? " "
                            + person.getFirstName() : "") + (person.getMiddleName() != null ? " " + person.getMiddleName() : ""),
                    person.getBirthDate() == null ? "__" : FastDateFormat.getInstance("dd.MM.yyyy").format(person.getBirthDate()),
                    makeOverlappingPersonsString(overlappingPersonList));

        }

        if (!(minDate == null || DateUtils.addDays(SimpleDateUtils.toStartOfDay(minDate), -1).equals(SimpleDateUtils.toStartOfDay(person.getRecordVersionTo()))
                || maxDate != null && DateUtils.addDays(SimpleDateUtils.toStartOfDay(maxDate), 1).equals(SimpleDateUtils.toStartOfDay(person.getVersion())))) {
            throw new ServiceException("Невозможно сохранить данные физического лица. Между периодом действия с %s по %s для версии %s ФЛ (%s) %s, %s и периодом действия других версий этого ФЛ имеется временной разрыв более 1 календарного дня.",
                    FastDateFormat.getInstance("dd.MM.yyyy").format(person.getVersion()),
                    person.getRecordVersionTo() == null ? "__" : FastDateFormat.getInstance("dd.MM.yyyy").format(person.getRecordVersionTo()),
                    person.getId(),
                    person.getRecordId(),
                    (person.getLastName() != null ? " " + person.getLastName() : "") + (person.getFirstName() != null ? " "
                            + person.getFirstName() : "") + (person.getMiddleName() != null ? " " + person.getMiddleName() : ""),
                    person.getBirthDate() == null ? "__" : FastDateFormat.getInstance("dd.MM.yyyy").format(person.getBirthDate()));
        }
    }

    private String makeOverlappingPersonsString(List<RegistryPersonDTO> overlappingPersons) {
        List<String> overlappingPersonStrings = new ArrayList<>(overlappingPersons.size());
        for (RegistryPersonDTO overlappingPerson : overlappingPersons) {
            overlappingPersonStrings.add(String.format(
                    "[%s, период действия с %s по %s]",
                    overlappingPerson.getId(),
                    FastDateFormat.getInstance("dd.MM.yyyy").format(overlappingPerson.getVersion()),
                    overlappingPerson.getRecordVersionTo() == null ? "__" : FastDateFormat.getInstance("dd.MM.yyyy").format(overlappingPerson.getRecordVersionTo())));
        }
        return Joiner.on(", ").join(overlappingPersonStrings);
    }

    @Override
    public CheckDulResult checkDul(String docCode, String docNumber) {
        CheckDulResult result = new CheckDulResult();
        String erasedNumber = docNumber.replaceAll("[^\\wА-Яа-яЁё]", "");
        if (docCode.equals("91")) {
            if (ScriptUtils.isUSSRIdDoc(docNumber)) {
                result.setErrorMessage("Значение для типа ДУЛ с кодом 91 в поле \"Серия и номер\" указаны реквизиты паспорта гражданина СССР. Паспорт гражданина СССР не является разрешенным документом, удостоверяющим личность.");
            }
        } else {
            result.setErrorMessage(ScriptUtils.checkDul(docCode, erasedNumber, "Серия и номер"));
            if (result.getErrorMessage() == null) {
                result.setFormattedNumber(ScriptUtils.formatDocNumber(docCode, erasedNumber));
            }
        }
        return result;
    }

    @Override
    @PreAuthorize("hasPermission(#requestingUser, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).VIEW_NSI)")
    public PagingResult<RefBookPerson> fetchOriginalDuplicatesCandidates(PagingParams pagingParams, RefBookPersonFilter filter, TAUser requestingUser) {
        if (filter == null) {
            return new PagingResult<>();
        }
        PagingResult<RefBookPerson> persons = refBookPersonDao.fetchOriginalDuplicatesCandidates(pagingParams, filter);
        forbidVipsDataByUserPermissions(persons, requestingUser);
        return persons;
    }

    @Override
    public void savePersons(List<RegistryPerson> personList) {
        List<PersonDocument> idDocs = new ArrayList<>();
        List<PersonIdentifier> idTaxPayers = new ArrayList<>();
        List<PersonTb> personTbs = new ArrayList<>();

        for (RegistryPerson person : personList) {
            person.setId(dbUtils.getNextRefBookRecordIds(1).get(0));
            Long recordId = dbUtils.getNextIds(DBUtils.Sequence.REF_BOOK_RECORD_ROW, 1).get(0);
            person.setRecordId(recordId);
            person.setOldId(recordId);

            idDocs.addAll(person.getDocuments());
            idTaxPayers.addAll(person.getPersonIdentityList());
            personTbs.addAll(person.getPersonTbList());
        }
        refBookPersonDao.saveBatch(personList);
        idDocDaoImpl.saveBatch(idDocs);
        idTaxPayerDaoImpl.saveBatch(idTaxPayers);
        personTbDaoImpl.saveBatch(personTbs);

        refBookPersonDao.updateBatch(personList);


    }

    /**
     * Подготавливает введенный текст для использования в SQL-запросе
     *
     * @param searchPattern текст для поиска
     * @return обработанный текст - исключены лишние пробелы, кавычки и т.д
     */
    private String prepareForQuery(String searchPattern) {
        return com.aplana.sbrf.taxaccounting.model.util.StringUtils.cleanString(searchPattern).toLowerCase().replaceAll("\'", "\\\\\'");
    }

    /**
     * Выбирает версию из списка версий по следующему правилу:
     * 1. Если есть актуальная версия, выбирает эту версию.
     * 2. Если нет актуальной версии, тогда:
     * a. Если все версии старше актуальной даты выбирает наименьшую
     * b. Если все версии младше актуальной даты выбирает наибольшую
     * Список версий ФЛ должен быть отсортирован по дате версии по убыванию
     *
     * @param descSortedRecords список версий ФЛ отсортированный по дате версии по убыванию
     * @param actualDate        актуальная дата по которой выбирается фверсия ФЛ
     * @return Выбранная версия физлица
     */
    private RegistryPersonDTO resolveVersion(List<RegistryPersonDTO> descSortedRecords, Date actualDate) {
        RegistryPersonDTO toReturn = null;
        for (RegistryPersonDTO record : descSortedRecords) {
            if (record.getVersion().compareTo(actualDate) <= 0) {
                if (record.getState() == 0) {
                    toReturn = record;
                    return toReturn;
                }
            } else {
                if (record.getState() == 0) {
                    toReturn = record;
                }
            }
        }
        return toReturn;
    }
}
