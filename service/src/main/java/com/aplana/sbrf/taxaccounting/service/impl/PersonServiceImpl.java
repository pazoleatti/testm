package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.async.AbstractStartupAsyncTaskHandler;
import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.dao.impl.components.RegistryPersonUpdateQueryBuilder;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookPersonDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.filter.refbook.RefBookPersonFilter;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;
import com.aplana.sbrf.taxaccounting.model.result.CheckDulResult;
import com.aplana.sbrf.taxaccounting.permissions.BasePermissionEvaluator;
import com.aplana.sbrf.taxaccounting.permissions.PersonVipDataPermission;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
    private static final Log LOG = LogFactory.getLog(PersonServiceImpl.class);

    @Autowired
    private RefBookPersonDao refBookPersonDao;
    @Autowired
    private CommonRefBookService commonRefBookService;
    @Autowired
    private RefBookFactory refBookFactory;
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
    BasePermissionEvaluator permissionEvaluator;
    @Autowired
    RegistryPersonUpdateQueryBuilder registryPersonUpdateBuilder;

    private static final ThreadLocal<SimpleDateFormat> formatter = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };

    @Override
    public RegistryPerson fetchOriginal(Long id, Date actualDate) {
        List<RegistryPerson> versions = refBookPersonDao.fetchOriginal(id);
        return resolveVersion(versions, actualDate);
    }

    @Override
    public String getPersonDocNumber(long personId) {
        return refBookPersonDao.getPersonDocNumber(personId);
    }

    @Override
    public PagingResult<RegistryPerson> fetchDuplicates(Long id, Date actualDate, PagingParams pagingParams) {
        List<RegistryPerson> toReturnData = new ArrayList<>();
        List<RegistryPerson> candidates = refBookPersonDao.fetchDuplicates(id, pagingParams);
        Map<Long, List<RegistryPerson>> candidatesGroupedByOldId = new HashMap<>();
        for (RegistryPerson candidate : candidates) {
            List<RegistryPerson> group = candidatesGroupedByOldId.get(candidate.getOldId());
            if (group == null) {
                group = new ArrayList<>();
                group.add(candidate);
                candidatesGroupedByOldId.put(candidate.getOldId(), group);
            } else {
                group.add(candidate);
            }
        }
        for (List<RegistryPerson> groupContent : candidatesGroupedByOldId.values()) {
            RegistryPerson person = resolveVersion(groupContent, actualDate);
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
    public ActionResult saveOriginalAndDuplicates(TAUserInfo userInfo, RefBookPerson currentPerson, RefBookPerson original, List<RefBookPerson> newDuplicates, List<RefBookPerson> deletedDuplicates) {
        // TODO: вся эта дичь ниже взята из старой реализации, ее надо переписать, но аналитики не нашли время чтобы сделать правки в постановке и синхронизировать ее с реализацией
        ActionResult result = new ActionResult();
        Logger logger = new Logger();
        logger.setTaUserInfo(userInfo);
        RefBookDataProvider refBookDataProvider = refBookFactory.getDataProvider(RefBook.Id.PERSON.getId());
        RefBookDataProvider dulDataProvider = refBookFactory.getDataProvider(RefBook.Id.ID_DOC.getId());

        List<RefBookPerson> duplicateRecords = newDuplicates;
        RefBookPerson originalMap;
        if (original != null) {
            // Проверка оригинала
            if (original.getOldId() != null) {
                throw new ServiceException("Выбранная оригиналом запись являеется дубликатом записи с \"Идентификатор ФЛ\" = %d", original.getOldId());
            }
            originalMap = original;
            duplicateRecords.add(currentPerson); // исходная запись является дубликатом
        } else {
            if (currentPerson.getOldId() != null) {
                setOriginal(Collections.singletonList(currentPerson.getOldId()));
            }
            originalMap = currentPerson; // исходная запись становиться оригиналом
        }
        Long originalRecordId = originalMap.getRecordId();
        List<Long> originalUniqueRecordIds = refBookDataProvider.getUniqueRecordIds(null, "RECORD_ID = " + originalRecordId);

        //набор ДУЛов оригинала
        Map<Long, Map<String, RefBookValue>> originalDulList = dulDataProvider.getRecordDataWhere("PERSON_ID = " + originalMap.getId());

        for (RefBookPerson duplicateRecord : duplicateRecords) {
            Long duplicateRecordId = duplicateRecord.getRecordId();
            Long duplicateOldId = null;
            if (duplicateRecord.getOldId() != null) {
                duplicateOldId = duplicateRecord.getOldId();
            }
            if (duplicateOldId == null) {
                // оригинал становиться дубликатом
                // нужно проверить дубликаты данной записи
                setDuplicate(Collections.singletonList(duplicateRecordId), originalRecordId);

                //набор ДУЛов дубликата
                Map<Long, Map<String, RefBookValue>> duplicateDulList = dulDataProvider.getRecordDataWhere("PERSON_ID = " + duplicateRecord.getId());
                List<RefBookRecord> newDulList = new ArrayList<RefBookRecord>();
                for (Map.Entry<Long, Map<String, RefBookValue>> entryDuplicate : duplicateDulList.entrySet()) {
                    boolean exist = false;
                    for (Map.Entry<Long, Map<String, RefBookValue>> entryOriginal : originalDulList.entrySet()) {
                        // проверяем существование ДУЛов у оригинала
                        if (compare(entryOriginal.getValue(), entryDuplicate.getValue())) {
                            exist = true;
                            break;
                        }
                    }
                    if (!exist) {
                        // Копируем ДУЛы
                        for (Long uniqueRecordId : originalUniqueRecordIds) {
                            RefBookRecord refBookRecord = new RefBookRecord();
                            refBookRecord.setValues(newDul(entryDuplicate.getValue(), uniqueRecordId, duplicateRecordId));
                            newDulList.add(refBookRecord);
                        }
                    }
                }

                if (!newDulList.isEmpty()) {
                    dulDataProvider.createRecordVersion(logger, new Date(), null, newDulList);
                }
            } else if (duplicateRecordId.equals(originalRecordId)) {
                // уже назначен дубликатом
            } else {
                // версия назначена дубликатом на другую запись???
                if (original == null) {
                    Long oldRecordId = currentPerson.getRecordId();
                    if (oldRecordId.equals(duplicateRecordId)) {
                        changeRecordId(Collections.singletonList(duplicateOldId), originalRecordId);
                    }
                }
            }
        }

        for (RefBookPerson duplicateRecord : deletedDuplicates) {
            Long duplicateRecordId = duplicateRecord.getRecordId();
            Long duplicateOldId = null;
            if (duplicateRecord.getOldId() != null) {
                duplicateOldId = duplicateRecord.getOldId();
            }
            if (duplicateOldId == null) {
                // уже является оригиналом
            } else if (duplicateRecordId.equals(originalRecordId)) {
                // является дубликатом данной записи
                setOriginal(Collections.singletonList(duplicateOldId));
            } else {
                // является дубликатом другой записи
            }
        }
        logger.info("Изменения успешно сохранены");
        result.setUuid(logEntryService.save(logger.getEntries()));
        return result;
    }

    private boolean compare(Map<String, RefBookValue> o1, Map<String, RefBookValue> o2) {
        if (!o1.get("DOC_ID").equals(o2.get("DOC_ID"))) {
            return false;
        }
        if (!o1.get("DOC_NUMBER").equals(o2.get("DOC_NUMBER"))) {
            return false;
        }
        return true;
    }

    private Map<String, RefBookValue> newDul(Map<String, RefBookValue> original, Long originalId, Long duplicateRecordId) {
        Map<String, RefBookValue> newDul = new HashMap<String, RefBookValue>();
        newDul.put("DOC_ID", original.get("DOC_ID"));
        newDul.put("DOC_NUMBER", original.get("DOC_NUMBER"));
        newDul.put("ISSUED_BY", original.get("ISSUED_BY"));
        newDul.put("ISSUED_DATE", original.get("ISSUED_DATE"));
        newDul.put("INC_REP", new RefBookValue(RefBookAttributeType.NUMBER, 0));
        newDul.put("PERSON_ID", new RefBookValue(RefBookAttributeType.REFERENCE, originalId));
        if (original.get("DUPLICATE_RECORD_ID").getNumberValue() != null) {
            newDul.put("DUPLICATE_RECORD_ID", original.get("DUPLICATE_RECORD_ID"));
        } else {
            newDul.put("DUPLICATE_RECORD_ID", new RefBookValue(RefBookAttributeType.NUMBER, duplicateRecordId));
        }
        return newDul;
    }

    @Override
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).VIEW_NSI)")
    public void setOriginal(List<Long> recordIds) {
        LOG.info(String.format("PersonServiceImpl.setOriginal. recordIds: %s", recordIds));
        refBookPersonDao.setOriginal(recordIds);
    }

    @Override
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).VIEW_NSI)")
    public void setDuplicate(List<Long> recordIds, Long originalId) {
        LOG.info(String.format("PersonServiceImpl.setDuplicate. recordIds: %s; originalId: %s", recordIds, originalId));
        refBookPersonDao.setDuplicate(recordIds, originalId);
    }

    @Override
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).VIEW_NSI)")
    public void changeRecordId(List<Long> recordIds, Long originalId) {
        LOG.info(String.format("PersonServiceImpl.changeRecordId. recordIds: %s; originalId: %s", recordIds, originalId));
        refBookPersonDao.changeRecordId(recordIds, originalId);
    }

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
    public RegistryPerson fetchPerson(Long id) {

        RegistryPerson person = refBookPersonDao.fetchPersonWithVersionInfo(id);
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
        PagingResult<Map<String, RefBookValue>> result = refBookDao.getRecords(refBookId, actualRefBook.getTableName(), pagingParams, null, null, "person_id in (" + StringUtils.join(versionIds, ", ") + ") AND status = 0");
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
    public void updateRegistryPerson(RegistryPerson person) {
        RegistryPerson persistedPerson = fetchPerson(person.getId());
        List<RegistryPerson.UpdatableField> personFieldsToUpdate = new ArrayList<>();
        boolean viewVipDataGranted = permissionEvaluator.hasPermission(SecurityContextHolder.getContext().getAuthentication(), person, PersonVipDataPermission.VIEW_VIP_DATA);

        person.setVersion(SimpleDateUtils.toStartOfDay(person.getVersion()));
        persistedPerson.setVersion(SimpleDateUtils.toStartOfDay(persistedPerson.getVersion()));

        if (!person.getVersion().equals(persistedPerson.getVersion()))
            personFieldsToUpdate.add(RegistryPerson.UpdatableField.VERSION);
        if (!Optional.fromNullable(person.getLastName()).equals(Optional.fromNullable(persistedPerson.getLastName())))
            personFieldsToUpdate.add(RegistryPerson.UpdatableField.LAST_NAME);
        if (!Optional.fromNullable(person.getFirstName()).equals(Optional.fromNullable(persistedPerson.getFirstName())))
            personFieldsToUpdate.add(RegistryPerson.UpdatableField.FIRST_NAME);
        if (!Optional.fromNullable(person.getMiddleName()).equals(Optional.fromNullable(persistedPerson.getMiddleName())))
            personFieldsToUpdate.add(RegistryPerson.UpdatableField.MIDDLE_NAME);
        if (!Optional.fromNullable(SimpleDateUtils.toStartOfDay(person.getBirthDate())).equals(Optional.fromNullable(SimpleDateUtils.toStartOfDay(persistedPerson.getBirthDate()))))
            personFieldsToUpdate.add(RegistryPerson.UpdatableField.BIRTH_DATE);
        if (!Optional.fromNullable(person.getCitizenship()).equals(Optional.fromNullable(persistedPerson.getCitizenship())))
            personFieldsToUpdate.add(RegistryPerson.UpdatableField.CITIZENSHIP);
        if (!Optional.fromNullable(person.getSource()).equals(Optional.fromNullable(persistedPerson.getSource())))
            personFieldsToUpdate.add(RegistryPerson.UpdatableField.SOURCE);
        if (viewVipDataGranted) {
            if (!Optional.fromNullable(person.getReportDoc()).equals(Optional.fromNullable(persistedPerson.getReportDoc())))
                personFieldsToUpdate.add(RegistryPerson.UpdatableField.REPORT_DOC);
            if (!Optional.fromNullable(person.getInn()).equals(Optional.fromNullable(persistedPerson.getInn())))
                personFieldsToUpdate.add(RegistryPerson.UpdatableField.INN);
            if (!Optional.fromNullable(person.getInnForeign()).equals(Optional.fromNullable(persistedPerson.getInnForeign())))
                personFieldsToUpdate.add(RegistryPerson.UpdatableField.INN_FOREIGN);
            if (!Optional.fromNullable(person.getSnils()).equals(Optional.fromNullable(persistedPerson.getSnils())))
                personFieldsToUpdate.add(RegistryPerson.UpdatableField.SNILS);
            if (!Optional.fromNullable(person.getTaxPayerState()).equals(Optional.fromNullable(persistedPerson.getTaxPayerState())))
                personFieldsToUpdate.add(RegistryPerson.UpdatableField.TAX_PAYER_STATE);
            if (!person.getVip() == persistedPerson.getVip())
                personFieldsToUpdate.add(RegistryPerson.UpdatableField.VIP);
        }

        List<RegistryPerson.UpdatableField> addressFieldsToUpdate = new ArrayList<>();
        if (viewVipDataGranted) {
            String newRegionCode = person.getAddress() != null && person.getAddress().value() != null ? person.getAddress().value().get(RegistryPerson.UpdatableField.REGION_CODE.getAlias()).getStringValue() : null;
            String oldRegionCode = persistedPerson.getAddress() != null && persistedPerson.getAddress().value() != null ? persistedPerson.getAddress().value().get(RegistryPerson.UpdatableField.REGION_CODE.getAlias()).getStringValue() : null;
            String newPostalCode = person.getAddress() != null && person.getAddress().value() != null ? person.getAddress().value().get(RegistryPerson.UpdatableField.POSTAL_CODE.getAlias()).getStringValue() : null;
            String oldPostalCode = persistedPerson.getAddress() != null && persistedPerson.getAddress().value() != null ? persistedPerson.getAddress().value().get(RegistryPerson.UpdatableField.POSTAL_CODE.getAlias()).getStringValue() : null;
            String newDistrict = person.getAddress() != null && person.getAddress().value() != null ? person.getAddress().value().get(RegistryPerson.UpdatableField.DISTRICT.getAlias()).getStringValue() : null;
            String oldDistrict = persistedPerson.getAddress() != null && persistedPerson.getAddress().value() != null ? persistedPerson.getAddress().value().get(RegistryPerson.UpdatableField.DISTRICT.getAlias()).getStringValue() : null;
            String newCity = person.getAddress() != null && person.getAddress().value() != null ? person.getAddress().value().get(RegistryPerson.UpdatableField.CITY.getAlias()).getStringValue() : null;
            String oldCity = persistedPerson.getAddress() != null && persistedPerson.getAddress().value() != null ? persistedPerson.getAddress().value().get(RegistryPerson.UpdatableField.CITY.getAlias()).getStringValue() : null;
            String newLocality = person.getAddress() != null && person.getAddress().value() != null ? person.getAddress().value().get(RegistryPerson.UpdatableField.LOCALITY.getAlias()).getStringValue() : null;
            String oldLocality = persistedPerson.getAddress() != null && persistedPerson.getAddress().value() != null ? persistedPerson.getAddress().value().get(RegistryPerson.UpdatableField.LOCALITY.getAlias()).getStringValue() : null;
            String newStreet = person.getAddress() != null && person.getAddress().value() != null ? person.getAddress().value().get(RegistryPerson.UpdatableField.STREET.getAlias()).getStringValue() : null;
            String oldStreet = persistedPerson.getAddress() != null && persistedPerson.getAddress().value() != null ? persistedPerson.getAddress().value().get(RegistryPerson.UpdatableField.STREET.getAlias()).getStringValue() : null;
            String newHouse = person.getAddress() != null && person.getAddress().value() != null ? person.getAddress().value().get(RegistryPerson.UpdatableField.HOUSE.getAlias()).getStringValue() : null;
            String oldHouse = persistedPerson.getAddress() != null && persistedPerson.getAddress().value() != null ? persistedPerson.getAddress().value().get(RegistryPerson.UpdatableField.HOUSE.getAlias()).getStringValue() : null;
            String newBuild = person.getAddress() != null && person.getAddress().value() != null ? person.getAddress().value().get(RegistryPerson.UpdatableField.BUILD.getAlias()).getStringValue() : null;
            String oldBuild = persistedPerson.getAddress() != null && persistedPerson.getAddress().value() != null ? persistedPerson.getAddress().value().get(RegistryPerson.UpdatableField.BUILD.getAlias()).getStringValue() : null;
            String newAppartment = person.getAddress() != null && person.getAddress().value() != null ? person.getAddress().value().get(RegistryPerson.UpdatableField.APPARTMENT.getAlias()).getStringValue() : null;
            String oldAppartment = persistedPerson.getAddress() != null && persistedPerson.getAddress().value() != null ? persistedPerson.getAddress().value().get(RegistryPerson.UpdatableField.APPARTMENT.getAlias()).getStringValue() : null;
            Long newCountryId = person.getAddress() != null && person.getAddress().value() != null ? person.getAddress().value().get(RegistryPerson.UpdatableField.COUNTRY_ID.getAlias()).getReferenceValue() : null;
            Long oldCountryId = persistedPerson.getAddress() != null && persistedPerson.getAddress().value() != null && persistedPerson.getAddress().value().get(RegistryPerson.UpdatableField.COUNTRY_ID.getAlias()).getValue() != null ? ((Map<String, RefBookValue>) persistedPerson.getAddress().value().get(RegistryPerson.UpdatableField.COUNTRY_ID.getAlias()).getValue()).get("id").getNumberValue().longValue() : null;
            String newAddress = person.getAddress() != null && person.getAddress().value() != null ? person.getAddress().value().get(RegistryPerson.UpdatableField.ADDRESS.getAlias()).getStringValue() : null;
            String oldAddress = persistedPerson.getAddress() != null && persistedPerson.getAddress().value() != null ? persistedPerson.getAddress().value().get(RegistryPerson.UpdatableField.ADDRESS.getAlias()).getStringValue() : null;


            if ((newRegionCode != null && !newRegionCode.equalsIgnoreCase(oldRegionCode))
                    || (newRegionCode == null && oldRegionCode != null))
                addressFieldsToUpdate.add(RegistryPerson.UpdatableField.REGION_CODE);
            if ((newPostalCode != null && !newPostalCode.equalsIgnoreCase(oldPostalCode))
                    || (newPostalCode == null && oldPostalCode != null))
                addressFieldsToUpdate.add(RegistryPerson.UpdatableField.POSTAL_CODE);
            if ((newDistrict != null && !newDistrict.equalsIgnoreCase(oldDistrict))
                    || (newDistrict == null && oldDistrict != null))
                addressFieldsToUpdate.add(RegistryPerson.UpdatableField.DISTRICT);
            if ((newCity != null && !newCity.equalsIgnoreCase(oldCity))
                    || (newCity == null && oldCity != null))
                addressFieldsToUpdate.add(RegistryPerson.UpdatableField.CITY);
            if ((newLocality != null && !newLocality.equalsIgnoreCase(oldLocality))
                    || (newLocality == null && oldLocality != null))
                addressFieldsToUpdate.add(RegistryPerson.UpdatableField.LOCALITY);
            if ((newStreet != null && !newStreet.equalsIgnoreCase(oldStreet))
                    || (newStreet == null && oldStreet != null))
                addressFieldsToUpdate.add(RegistryPerson.UpdatableField.STREET);
            if ((newHouse != null && !newHouse.equalsIgnoreCase(oldHouse))
                    || (newHouse == null && oldHouse != null))
                addressFieldsToUpdate.add(RegistryPerson.UpdatableField.HOUSE);
            if ((newBuild != null && !newBuild.equalsIgnoreCase(oldBuild))
                    || (newBuild == null && oldBuild != null))
                addressFieldsToUpdate.add(RegistryPerson.UpdatableField.BUILD);
            if ((newAppartment != null && !newAppartment.equalsIgnoreCase(oldAppartment))
                    || (newAppartment == null && oldAppartment != null))
                addressFieldsToUpdate.add(RegistryPerson.UpdatableField.APPARTMENT);
            if ((newCountryId != null && !newCountryId.equals(oldCountryId))
                    || (newCountryId == null && oldCountryId != null))
                addressFieldsToUpdate.add(RegistryPerson.UpdatableField.COUNTRY_ID);
            if ((newAddress != null && !newAddress.equalsIgnoreCase(oldAddress))
                    || (newAddress == null && oldAddress != null))
                addressFieldsToUpdate.add(RegistryPerson.UpdatableField.ADDRESS);
        }

        if (person.getRecordVersionTo() != null) {
            if (!person.getRecordVersionTo().equals(persistedPerson.getRecordVersionTo())) {
                refBookPersonDao.deleteRegistryPersonFakeVersion(person.getRecordId());
                person.setVersionEnd(SimpleDateUtils.toStartOfDay(persistedPerson.getRecordVersionTo()));
                refBookPersonDao.saveRegistryPersonFakeVersion(person);
            }
        } else {
            if (persistedPerson.getRecordVersionTo() != null) {
                refBookPersonDao.deleteRegistryPersonFakeVersion(person.getRecordId());
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

        if (personFieldsToUpdate.contains(RegistryPerson.UpdatableField.REPORT_DOC)) {
            refBookPersonDao.updateRegistryPersonIncRepDocId(persistedPerson.getReportDoc().value().get("id").getNumberValue().longValue(), person.getReportDoc().value().get("id").getNumberValue().longValue());
        }
    }

    @Override
    public void checkVersionOverlapping(RegistryPerson person) {
        Date minDate = null, maxDate = new Date(0);
        List<RegistryPerson> overlappingPersonList = new ArrayList<>();
        RefBookPersonFilter filter = new RefBookPersonFilter();
        filter.setId(person.getRecordId().toString());
        List<RegistryPerson> relatedPersons = refBookPersonDao.fetchNonDuplicatesVersions(person.getRecordId());
        for (RegistryPerson relatedPerson : relatedPersons) {
            if (person.getId() == null || !person.getId().equals(relatedPerson.getId())) {
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
            throw new ServiceException("Период действия с %s по %s для версии %s ФЛ (%s)%s, %s пересекается с периодом действия других версий этого ФЛ: %s",
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
            throw new ServiceException("Между периодом действия с %s по %s для версии %s ФЛ (%s) %s, %s и периодом действия других версий этого ФЛ имеется временной разрыв более 1 календарного дня.",
                    FastDateFormat.getInstance("dd.MM.yyyy").format(person.getVersion()),
                    person.getRecordVersionTo() == null ? "__" : FastDateFormat.getInstance("dd.MM.yyyy").format(person.getRecordVersionTo()),
                    person.getId(),
                    person.getRecordId(),
                    (person.getLastName() != null ? " " + person.getLastName() : "") + (person.getFirstName() != null ? " "
                            + person.getFirstName() : "") + (person.getMiddleName() != null ? " " + person.getMiddleName() : ""),
                    person.getBirthDate() == null ? "__" : FastDateFormat.getInstance("dd.MM.yyyy").format(person.getBirthDate()));
        }
    }

    private String makeOverlappingPersonsString(List<RegistryPerson> overlappingPersons) {
        List<String> overlappingPersonStrings = new ArrayList<>(overlappingPersons.size());
        for (RegistryPerson overlappingPerson : overlappingPersons) {
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
    private RegistryPerson resolveVersion(List<RegistryPerson> descSortedRecords, Date actualDate) {
        RegistryPerson toReturn = null;
        for (RegistryPerson record : descSortedRecords) {
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
