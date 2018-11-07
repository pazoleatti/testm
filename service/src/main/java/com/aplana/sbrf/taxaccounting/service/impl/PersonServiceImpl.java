package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.async.AbstractStartupAsyncTaskHandler;
import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.dao.impl.IdDocDaoImpl;
import com.aplana.sbrf.taxaccounting.dao.impl.IdTaxPayerDaoImpl;
import com.aplana.sbrf.taxaccounting.dao.impl.PersonTbDaoImpl;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookPersonDao;
import com.aplana.sbrf.taxaccounting.dao.util.DBUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.filter.refbook.RefBookPersonFilter;
import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;
import com.aplana.sbrf.taxaccounting.model.result.CheckDulResult;
import com.aplana.sbrf.taxaccounting.permissions.BasePermissionEvaluator;
import com.aplana.sbrf.taxaccounting.permissions.PersonVipDataPermission;
import com.aplana.sbrf.taxaccounting.script.service.util.ScriptUtils;
import com.aplana.sbrf.taxaccounting.service.LockDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.PersonService;
import com.aplana.sbrf.taxaccounting.utils.SimpleDateUtils;
import com.google.common.base.Joiner;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Сервис работы Физическими лицами. Заменяет некоторые операции провайдера справочников для лучшей производительности
 */
@Service
public class PersonServiceImpl implements PersonService {

    @Autowired
    private RefBookPersonDao refBookPersonDao;
    @Autowired
    private LogEntryService logEntryService;
    @Autowired
    private LockDataService lockDataService;
    @Autowired
    private AsyncManager asyncManager;
    @Autowired
    private BasePermissionEvaluator permissionEvaluator;
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
    @Transactional (readOnly = true)
    public PagingResult<RegistryPersonDTO> getPersonsData(PagingParams pagingParams, RefBookPersonFilter filter) {
        PagingResult<RegistryPerson> persons = refBookPersonDao.getPersons(pagingParams, filter);
        PagingResult<RegistryPersonDTO> result = convertPersonToDTO(persons, persons.getTotalCount());
        forbidVipsDataByUserPermissions(result);
        return result;
    }

    private PagingResult<RegistryPersonDTO> convertPersonToDTO(Collection<RegistryPerson> fromList, int count) {
        PagingResult<RegistryPersonDTO> dtoList = new PagingResult<>();
        for (RegistryPerson fromObj : fromList) {
            if (fromObj != null) {
                RegistryPersonDTO dto = new RegistryPersonDTO();
                BeanUtils.copyProperties(fromObj, dto, "reportDoc", "citizenship", "inn", "innForeign", "snils", "taxPayerState", "address", "documents");
                dto.setReportDoc(Permissive.of(fromObj.getReportDoc()));
                dto.setCitizenship(Permissive.of(fromObj.getCitizenship()));
                dto.setInn(Permissive.of(fromObj.getInn()));
                dto.setInnForeign(Permissive.of(fromObj.getInnForeign()));
                dto.setSnils(Permissive.of(fromObj.getSnils()));
                dto.setAddress(Permissive.of(fromObj.getAddress()));
                dto.setTaxPayerState(Permissive.of(fromObj.getTaxPayerState()));
                dto.setDocuments(Permissive.of(fromObj.getDocuments()));
                dto.setId(fromObj.getId());
                RegistryPerson stub = new RegistryPerson();
                stub.setId(fromObj.getId());
                for (PersonIdentifier personIdentifier : dto.getPersonIdentityList()) {
                    personIdentifier.setPerson(stub);
                }
                for (PersonTb personTb : dto.getPersonTbList()) {
                    personTb.setPerson(stub);
                }
                dtoList.add(dto);
            }
        }
        dtoList.setTotalCount(count);
        return dtoList;
    }

    private void forbidVipsDataByUserPermissions(List<RegistryPersonDTO> persons) {
        for (RegistryPersonDTO person : persons) {
            boolean viewVipDataGranted = permissionEvaluator.hasPermission(SecurityContextHolder.getContext().getAuthentication(), person, PersonVipDataPermission.VIEW_VIP_DATA);
            if (!viewVipDataGranted) {
                person.setReportDoc(Permissive.<IdDoc>forbidden());
                person.setInn(Permissive.<String>forbidden());
                person.setInnForeign(Permissive.<String>forbidden());
                person.setSnils(Permissive.<String>forbidden());
                person.setAddress(Permissive.<Address>forbidden());
                person.setDocuments(Permissive.<List<IdDoc>>forbidden());
            }
        }
    }

    @Override
    @Transactional (readOnly = true)
    public int getPersonsCount(RefBookPersonFilter filter) {
        return refBookPersonDao.getPersonsCount(filter);
    }

    @Override
    public RegistryPersonDTO fetchPersonData(Long id) {
        final RegistryPerson person = refBookPersonDao.fetchPersonVersion(id);
        person.setDocuments(idDocDaoImpl.getByPerson(person));
        person.setPersonIdentityList(idTaxPayerDaoImpl.getByPerson(person));
        person.setPersonTbList(personTbDaoImpl.getByPerson(person));
        List<RegistryPersonDTO> resultData = convertPersonToDTO(Collections.singletonList(person), 1);
        forbidVipsDataByUserPermissions(resultData);
        RegistryPersonDTO result = resultData.get(0);

        List<RegistryPerson> versions = refBookPersonDao.fetchOriginal(id);
        RegistryPerson original = resolveVersion(versions, new Date());

        RegistryPersonDTO originalData = null;
        if (original != null) {
            originalData = convertPersonToDTO(Collections.singletonList(original), 1).get(0);
            forbidVipsDataByUserPermissions(Collections.singletonList(originalData));
        }
        result.setOriginal(originalData);

        if (original == null) {
            List<RegistryPerson> candidates = refBookPersonDao.fetchDuplicates(id);
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

            List<RegistryPerson> pickedDuplicates = new ArrayList<>();
            for (List<RegistryPerson> groupContent : candidatesGroupedByOldId.values()) {
                RegistryPerson duplicate = resolveVersion(groupContent, new Date());
                pickedDuplicates.add(duplicate);
            }
            List<RegistryPersonDTO> duplicatesData = convertPersonToDTO(pickedDuplicates, pickedDuplicates.size());
            forbidVipsDataByUserPermissions(duplicatesData);
            result.setDuplicates(duplicatesData);
        }

        return result;
    }

    @Override
    @PreAuthorize("hasPermission(#userInfo.user, T(com.aplana.sbrf.taxaccounting.permissions.UserPermission).EXPORT_PERSONS)")
    @Transactional
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
    @Transactional
    public void updateRegistryPerson(RegistryPersonDTO person) {
        checkVersionOverlapping(person);
        final RegistryPerson persistedPerson = new RegistryPerson();

        List<IdDoc> idDocsToCreate = new ArrayList<>();
        List<IdDoc> idDocsToUpdate = new ArrayList<>();
        List<Long> idDocsToDelete = new ArrayList<>();

        persistedPerson.setRecordId(person.getRecordId());

        List<IdDoc> persistedIdDocs = idDocDaoImpl.getByPerson(persistedPerson);

        for (IdDoc persistedIdDoc : persistedIdDocs) {
            idDocsToDelete.add(persistedIdDoc.getId());
        }

        for (IdDoc idDoc : person.getDocuments().value()) {
            if (idDoc.getId() == null) {
                idDocsToCreate.add(idDoc);
            } else {
                idDocsToUpdate.add(idDoc);
                idDocsToDelete.remove(idDoc.getId());
            }
        }

        if (CollectionUtils.isNotEmpty(idDocsToCreate)) {
            idDocDaoImpl.saveBatch(idDocsToCreate);
        }
        if (CollectionUtils.isNotEmpty(idDocsToUpdate)) {
            idDocDaoImpl.updateBatch(idDocsToUpdate);
        }
        if (CollectionUtils.isNotEmpty(idDocsToDelete)) {
            idDocDaoImpl.deleteByIds(idDocsToDelete);
        }

        if (person.getOriginal() == null) {
            persistedPerson.setRecordId(person.getOldId());
        } else {
            persistedPerson.setRecordId(person.getOriginal().getRecordId());
            refBookPersonDao.setOriginal(person.getOriginal().getRecordId(), person.getRecordId());
        }

        List<Long> deletedDuplicates = new ArrayList<>();
        List<Long> duplicates = new ArrayList<>();
        if (person.getOriginal() == null && CollectionUtils.isNotEmpty(person.getDuplicates())) {
            for (RegistryPersonDTO duplicate : person.getDuplicates()) {

                if (duplicate.getRecordId().equals(duplicate.getOldId())) {
                    deletedDuplicates.add(duplicate.getRecordId());
                } else {
                    duplicates.add(duplicate.getOldId());
                }
            }
        }
        if (CollectionUtils.isNotEmpty(duplicates)) {
            refBookPersonDao.setDuplicates(duplicates, person.getRecordId());
        }

        if (CollectionUtils.isNotEmpty(deletedDuplicates)) {
            refBookPersonDao.deleteDuplicates(deletedDuplicates);
        }

        persistedPerson.setId(person.getId());
        persistedPerson.setOldId(person.getOldId());
        persistedPerson.setStartDate(SimpleDateUtils.toStartOfDay(person.getStartDate()));
        persistedPerson.setEndDate(SimpleDateUtils.toStartOfDay(person.getEndDate()));
        persistedPerson.setLastName(person.getLastName());
        persistedPerson.setFirstName(person.getFirstName());
        persistedPerson.setMiddleName(person.getMiddleName());
        persistedPerson.setBirthDate(SimpleDateUtils.toStartOfDay(person.getBirthDate()));
        persistedPerson.setCitizenship(person.getCitizenship().value() != null ? person.getCitizenship().value() : new RefBookCountry());
        persistedPerson.setSource(person.getSource() != null ? person.getSource() : new RefBookAsnu());
        if (person.getReportDoc().value() != null) {
            IdDoc idDoc = person.getReportDoc().value();
            if (idDoc.getId() == null) {
                persistedIdDocs = idDocDaoImpl.getByPerson(persistedPerson);
            }
            for (IdDoc persistedIdDoc : persistedIdDocs) {
                if (persistedIdDoc.getDocumentNumber().equals(idDoc.getDocumentNumber()) && persistedIdDoc.getDocType().getCode().equals(idDoc.getDocType().getCode())) {
                    idDoc.setId(persistedIdDoc.getId());
                }
            }
            persistedPerson.setReportDoc(idDoc);
        }
        persistedPerson.setInn(person.getInn().value());
        persistedPerson.setInnForeign(person.getInnForeign().value());
        persistedPerson.setSnils(person.getSnils().value());
        persistedPerson.setTaxPayerState(person.getTaxPayerState().value() != null ? person.getTaxPayerState().value() : new RefBookTaxpayerState());
        persistedPerson.setVip(person.isVip());
        persistedPerson.getAddress().setRegionCode(person.getAddress().value().getRegionCode());
        persistedPerson.getAddress().setPostalCode(person.getAddress().value().getPostalCode());
        persistedPerson.getAddress().setDistrict(person.getAddress().value().getDistrict());
        persistedPerson.getAddress().setCity(person.getAddress().value().getCity());
        persistedPerson.getAddress().setLocality(person.getAddress().value().getLocality());
        persistedPerson.getAddress().setStreet(person.getAddress().value().getStreet());
        persistedPerson.getAddress().setHouse(person.getAddress().value().getHouse());
        persistedPerson.getAddress().setBuild(person.getAddress().value().getBuild());
        persistedPerson.getAddress().setAppartment(person.getAddress().value().getAppartment());
        persistedPerson.getAddress().setCountry(person.getAddress().value().getCountry() != null ? person.getAddress().value().getCountry() : new RefBookCountry());
        persistedPerson.getAddress().setAddressIno(person.getAddress().value().getAddressIno());

        refBookPersonDao.updateRegistryPerson(persistedPerson);
    }

    @Override
    @Transactional
    public void updateIdentificatedPersons(List<NaturalPerson> personList) {
        List<IdDoc> idDocs = new ArrayList<>();
        List<PersonIdentifier> idTaxPayers = new ArrayList<>();
        List<PersonTb> personTbs = new ArrayList<>();

        for (NaturalPerson person : personList) {
            idDocs.addAll(person.getDocuments());
            idTaxPayers.addAll(person.getPersonIdentityList());
            personTbs.addAll(person.getPersonTbList());
        }

        List<RegistryPerson> toSave = new ArrayList<>();
        for (NaturalPerson person : personList) {
            toSave.add(person);
        }

        List<IdDoc> idDocToSave = new ArrayList<>();
        List<IdDoc> idDocToUpdate = new ArrayList<>();
        for (IdDoc idDoc : idDocs) {
            if (idDoc.getId() == null) {
                idDocToSave.add(idDoc);
            } else {
                idDocToUpdate.add(idDoc);
            }
        }

        refBookPersonDao.updateBatch(toSave);
        idDocDaoImpl.saveBatch(idDocToSave);
        idDocDaoImpl.updateBatch(idDocToUpdate);
        idTaxPayerDaoImpl.saveBatch(idTaxPayers);
        personTbDaoImpl.saveBatch(personTbs);

        refBookPersonDao.updateBatch(toSave);
    }

    void checkVersionOverlapping(RegistryPersonDTO person) {
        Date minDate = null, maxDate = new Date(0);
        List<RegistryPerson> overlappingPersonList = new ArrayList<>();
        RefBookPersonFilter filter = new RefBookPersonFilter();
        filter.setId(person.getRecordId().toString());
        List<RegistryPerson> relatedPersons = refBookPersonDao.fetchNonDuplicatesVersions(person.getRecordId());
        for (RegistryPerson relatedPerson : relatedPersons) {
            if (person.getId() == null || !person.getId().equals(relatedPerson.getId()) && person.getRecordId().equals(person.getOldId())) {
                // Проверка пересечения существующей с исходной
                if (!(SimpleDateUtils.toStartOfDay(relatedPerson.getEndDate()) != null && SimpleDateUtils.toStartOfDay(person.getStartDate()).after(SimpleDateUtils.toStartOfDay(relatedPerson.getEndDate()))
                        || SimpleDateUtils.toStartOfDay(person.getStartDate()).before(SimpleDateUtils.toStartOfDay(relatedPerson.getStartDate()))
                        && person.getStartDate() != null && SimpleDateUtils.toStartOfDay(person.getEndDate()).before(SimpleDateUtils.toStartOfDay(relatedPerson.getStartDate())))) {
                    overlappingPersonList.add(relatedPerson);
                }
                if (minDate == null || SimpleDateUtils.toStartOfDay(relatedPerson.getStartDate()).before(SimpleDateUtils.toStartOfDay(minDate))) {
                    minDate = relatedPerson.getStartDate();
                }
                if (maxDate != null && (SimpleDateUtils.toStartOfDay(relatedPerson.getEndDate()) == null || SimpleDateUtils.toStartOfDay(relatedPerson.getEndDate()).after(SimpleDateUtils.toStartOfDay(maxDate)))) {
                    maxDate = relatedPerson.getEndDate();
                }
            }
        }
        if (!overlappingPersonList.isEmpty()) {
            throw new ServiceException("Невозможно сохранить данные физического лица. Период действия с %s по %s для версии %s ФЛ (%s)%s, %s пересекается с периодом действия других версий этого ФЛ: %s",
                    FastDateFormat.getInstance("dd.MM.yyyy").format(person.getStartDate()),
                    person.getEndDate() == null ? "__" : FastDateFormat.getInstance("dd.MM.yyyy").format(person.getEndDate()),
                    person.getId(),
                    person.getRecordId(),
                    (person.getLastName() != null ? " " + person.getLastName() : "") + (person.getFirstName() != null ? " "
                            + person.getFirstName() : "") + (person.getMiddleName() != null ? " " + person.getMiddleName() : ""),
                    person.getBirthDate() == null ? "__" : FastDateFormat.getInstance("dd.MM.yyyy").format(person.getBirthDate()),
                    makeOverlappingPersonsString(overlappingPersonList));

        }

        if (!(minDate == null || DateUtils.addDays(SimpleDateUtils.toStartOfDay(minDate), -1).equals(SimpleDateUtils.toStartOfDay(person.getEndDate()))
                || maxDate != null && DateUtils.addDays(SimpleDateUtils.toStartOfDay(maxDate), 1).equals(SimpleDateUtils.toStartOfDay(person.getStartDate())))) {
            throw new ServiceException("Невозможно сохранить данные физического лица. Между периодом действия с %s по %s для версии %s ФЛ (%s) %s, %s и периодом действия других версий этого ФЛ имеется временной разрыв более 1 календарного дня.",
                    FastDateFormat.getInstance("dd.MM.yyyy").format(person.getStartDate()),
                    person.getEndDate() == null ? "__" : FastDateFormat.getInstance("dd.MM.yyyy").format(person.getEndDate()),
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
                    FastDateFormat.getInstance("dd.MM.yyyy").format(overlappingPerson.getStartDate()),
                    overlappingPerson.getEndDate() == null ? "__" : FastDateFormat.getInstance("dd.MM.yyyy").format(overlappingPerson.getEndDate())));
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
    @Transactional (readOnly = true)
    public PagingResult<RegistryPersonDTO> fetchOriginalDuplicatesCandidates(PagingParams pagingParams, RefBookPersonFilter filter, TAUser requestingUser) {
        if (filter == null) {
            return new PagingResult<>();
        }
        PagingResult<RegistryPerson> persons = refBookPersonDao.fetchOriginalDuplicatesCandidates(pagingParams, filter);
        PagingResult<RegistryPersonDTO> result = convertPersonToDTO(persons, persons.getTotalCount());
        forbidVipsDataByUserPermissions(result);
        return result;
    }

    @Override
    @Transactional
    public List<RegistryPerson> savePersons(List<RegistryPerson> personList) {
        List<IdDoc> idDocs = new ArrayList<>();
        List<PersonIdentifier> idTaxPayers = new ArrayList<>();
        List<PersonTb> personTbs = new ArrayList<>();

        for (RegistryPerson person : personList) {
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

        return personList;
    }

    @Override
    @Transactional (readOnly = true)
    public List<RegistryPerson> findActualRefPersonsByDeclarationDataId(Long declarationDataId) {
        List<RegistryPerson> result = refBookPersonDao.findActualRefPersonsByDeclarationDataId(declarationDataId, new Date());
        for (RegistryPerson person : result) {
            person.getPersonIdentityList().addAll(idTaxPayerDaoImpl.getByPerson(person));
            person.getDocuments().addAll(idDocDaoImpl.getByPerson(person));
        }
        return result;
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
     * @param actualDate        актуальная дата по которой выбирается версия ФЛ
     * @return Выбранная версия физлица
     */
    private RegistryPerson resolveVersion(List<RegistryPerson> descSortedRecords, Date actualDate) {
        if (CollectionUtils.isNotEmpty(descSortedRecords)) {
            if (descSortedRecords.size() == 1) {
                return descSortedRecords.get(0);
            }
            for (RegistryPerson version : descSortedRecords) {
                if (version.getStartDate().compareTo(actualDate) <= 0 && version.getEndDate() == null || version.getEndDate().compareTo(actualDate) >= 0) {
                    return version;
                }
            }
            RegistryPerson first = descSortedRecords.get(0);
            RegistryPerson last = descSortedRecords.get(descSortedRecords.size() - 1);
            if (first.getEndDate() != null && first.getEndDate().compareTo(actualDate) < 0) {
                return first;
            }
            if (last.getStartDate().compareTo(actualDate) > 0) {
                return last;
            }
        }
        return null;
    }
}
