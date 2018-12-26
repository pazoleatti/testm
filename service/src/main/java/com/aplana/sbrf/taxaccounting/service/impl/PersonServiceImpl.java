package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.async.AbstractStartupAsyncTaskHandler;
import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.dao.impl.IdDocDaoImpl;
import com.aplana.sbrf.taxaccounting.dao.impl.IdTaxPayerDaoImpl;
import com.aplana.sbrf.taxaccounting.dao.impl.PersonTbDaoImpl;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookPersonDao;
import com.aplana.sbrf.taxaccounting.dao.util.DBUtils;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskType;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.Permissive;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.filter.refbook.RefBookPersonFilter;
import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.Address;
import com.aplana.sbrf.taxaccounting.model.refbook.IdDoc;
import com.aplana.sbrf.taxaccounting.model.refbook.PersonIdentifier;
import com.aplana.sbrf.taxaccounting.model.refbook.PersonTb;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAsnu;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookCountry;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookTaxpayerState;
import com.aplana.sbrf.taxaccounting.model.refbook.RegistryPerson;
import com.aplana.sbrf.taxaccounting.model.refbook.RegistryPersonDTO;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;
import com.aplana.sbrf.taxaccounting.model.result.CheckDulResult;
import com.aplana.sbrf.taxaccounting.permissions.BasePermissionEvaluator;
import com.aplana.sbrf.taxaccounting.permissions.PersonPermission;
import com.aplana.sbrf.taxaccounting.script.service.util.ScriptUtils;
import com.aplana.sbrf.taxaccounting.service.LockDataService;
import com.aplana.sbrf.taxaccounting.service.LogBusinessService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.PersonService;
import com.aplana.sbrf.taxaccounting.utils.SimpleDateUtils;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.aplana.sbrf.taxaccounting.model.util.IdentityObjectUtils.*;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

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
    @Autowired
    private LogBusinessService logBusinessService;

    @Override
    @Transactional(readOnly = true)
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
            boolean viewVipDataGranted = permissionEvaluator.hasPermission(SecurityContextHolder.getContext().getAuthentication(), person, PersonPermission.VIEW_VIP_DATA);
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
    @Transactional(readOnly = true)
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

        RegistryPerson original = findOriginal(id);
        RegistryPersonDTO originalData = null;
        if (original != null) {
            originalData = convertPersonToDTO(Collections.singletonList(original), 1).get(0);
            forbidVipsDataByUserPermissions(Collections.singletonList(originalData));
        }
        result.setOriginal(originalData);

        if (original == null) {
            List<RegistryPerson> duplicates = findAllDuplicates(id);
            List<RegistryPersonDTO> duplicatesData = convertPersonToDTO(duplicates, duplicates.size());
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
    @PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP')")
    @Transactional
    public void updateRegistryPerson(RegistryPersonDTO personDTO, TAUserInfo userInfo) {
        checkVersionOverlapping(personDTO);
        RegistryPerson persistedPerson = refBookPersonDao.fetchPersonVersion(personDTO.getId());
        RegistryPerson personToPersist = new RegistryPerson();
        personToPersist.setRecordId(personDTO.getRecordId());
        PersonChangeLogBuilder changeLogBuilder = new PersonChangeLogBuilder();

        // Обновляем ДУЛы
        List<IdDoc> persistedIdDocs = idDocDaoImpl.getByPerson(personToPersist);
        {
            List<IdDoc> idDocsToCreate = new ArrayList<>();
            List<IdDoc> idDocsToUpdate = new ArrayList<>();
            List<IdDoc> idDocsToDelete = new ArrayList<>(persistedIdDocs);

            for (IdDoc idDoc : personDTO.getDocuments().value()) {
                if (idDoc.getId() == null) {
                    idDocsToCreate.add(idDoc);
                } else {
                    if (!Objects.equal(idDoc.getId(), findById(persistedIdDocs, idDoc.getId()).getId())) {
                        idDocsToUpdate.add(idDoc);
                    }
                    removeById(idDocsToDelete, idDoc.getId());
                }
            }

            if (CollectionUtils.isNotEmpty(idDocsToCreate)) {
                idDocDaoImpl.createBatch(idDocsToCreate);
                for (IdDoc idDoc : idDocsToCreate) {
                    changeLogBuilder.dulCreated(idDoc);
                }
            }
            if (CollectionUtils.isNotEmpty(idDocsToUpdate)) {
                idDocDaoImpl.updateBatch(idDocsToUpdate);
                for (IdDoc idDoc : idDocsToUpdate) {
                    changeLogBuilder.dulUpdated(idDoc);
                }
            }
            if (CollectionUtils.isNotEmpty(idDocsToDelete)) {
                idDocDaoImpl.deleteByIds(getIds(idDocsToDelete));
                for (IdDoc idDoc : idDocsToDelete) {
                    changeLogBuilder.dulDeleted(idDoc);
                }
            }
        }

        RegistryPerson persistedOriginal = findOriginal(personDTO.getId());
        // Если оригинал изменился, то обновляем его
        if (!(persistedOriginal == null && personDTO.getOriginal() == null ||
                (persistedOriginal != null && persistedOriginal.getOldId().equals(personDTO.getOriginal() != null ? personDTO.getOriginal().getOldId() : null)))) {
            if (personDTO.getOriginal() != null) { // Установлен оригинал
                personToPersist.setRecordId(personDTO.getOriginal().getOldId());
                refBookPersonDao.setOriginal(personDTO.getOriginal().getOldId(), personDTO.getRecordId());
                changeLogBuilder.originalSet(personDTO.getOriginal());
            } else { // Удален оригинал
                personToPersist.setRecordId(personDTO.getOldId());
                changeLogBuilder.originalDeleted(persistedOriginal);
            }
        }

        // Обновляем дубликаты (только если не назначен оригинал, в противном случае дубликаты сотрутся)
        if (personDTO.getOriginal() == null) {
            List<RegistryPerson> persistedDuplicates = persistedOriginal == null ? findAllDuplicates(personDTO.getId()) : new ArrayList<RegistryPerson>();
            List<RegistryPerson> duplicatesToPersist = new ArrayList<>();
            for (RegistryPersonDTO duplicate : personDTO.getDuplicates()) {
                duplicatesToPersist.add(refBookPersonDao.fetchPersonVersion(duplicate.getId()));
            }
            List<RegistryPerson> deletedDuplicates = new ArrayList<>(persistedDuplicates);
            List<RegistryPerson> addedDuplicates = new ArrayList<>();
            for (RegistryPerson duplicate : duplicatesToPersist) {
                if (findByOldId(persistedDuplicates, duplicate.getOldId()) == null) {
                    addedDuplicates.add(duplicate);
                }
                removeByOldId(deletedDuplicates, duplicate.getOldId());
            }
            if (CollectionUtils.isNotEmpty(addedDuplicates)) {
                refBookPersonDao.setDuplicates(getOldIds(addedDuplicates), personDTO.getRecordId());
                changeLogBuilder.duplicatesSet(addedDuplicates);
            }
            if (CollectionUtils.isNotEmpty(deletedDuplicates)) {
                refBookPersonDao.deleteDuplicates(getOldIds(deletedDuplicates));
                changeLogBuilder.duplicatesDeleted(deletedDuplicates);
            }
        }

        List<PersonIdentifier> persistedInpList = idTaxPayerDaoImpl.getByPerson(personToPersist);
        {
            List<PersonIdentifier> inpToCreate = new ArrayList<>();
            List<PersonIdentifier> inpToDelete = new ArrayList<>(persistedInpList);
            List<PersonIdentifier> inpToUpdate = new ArrayList<>();

            for (PersonIdentifier inp : personDTO.getPersonIdentityList()) {
                if (inp.getId() == null) {
                    inpToCreate.add(inp);
                } else {
                    if (!Objects.equal(inp, findById(persistedInpList, inp.getId()))) {
                        inpToUpdate.add(inp);
                    }
                    removeById(inpToDelete, inp.getId());
                }
            }
            if (CollectionUtils.isNotEmpty(inpToCreate)) {
                idTaxPayerDaoImpl.createBatch(inpToCreate);
                for (PersonIdentifier inp : inpToCreate) {
                    changeLogBuilder.inpCreated(inp);
                }
            }
            if (CollectionUtils.isNotEmpty(inpToUpdate)) {
                idTaxPayerDaoImpl.updateBatch(inpToUpdate);
                for (PersonIdentifier inp : inpToUpdate) {
                    changeLogBuilder.inpUpdated(inp);
                }
            }
            if (CollectionUtils.isNotEmpty(inpToDelete)) {
                idTaxPayerDaoImpl.deleteByIds(getIds(inpToDelete));
                for (PersonIdentifier inp : inpToDelete) {
                    changeLogBuilder.inpDeleted(inp);
                }
            }
        }

        List<PersonTb> persistedTbList = personTbDaoImpl.getByPerson(personToPersist);
        {
            List<PersonTb> tbToCreate = new ArrayList<>();
            List<PersonTb> tbToDelete = new ArrayList<>(persistedTbList);
            List<PersonTb> tBToUpdate = new ArrayList<>();

            for (PersonTb editingPersonTb : personDTO.getPersonTbList()) {
                if (editingPersonTb.getId() == null) {
                    tbToCreate.add(editingPersonTb);
                } else {
                    PersonTb persistedPersonTb = findById(persistedTbList, editingPersonTb.getId());
                    if (persistedPersonTb != null && !Objects.equal(SimpleDateUtils.toStartOfDay(editingPersonTb.getImportDate()), SimpleDateUtils.toStartOfDay(persistedPersonTb.getImportDate()))) {
                        tBToUpdate.add(editingPersonTb);
                    }
                    removeById(tbToDelete, editingPersonTb.getId());
                }
            }
            if (CollectionUtils.isNotEmpty(tbToCreate)) {
                personTbDaoImpl.createBatch(tbToCreate);
                for (PersonTb personTb : tbToCreate) {
                    changeLogBuilder.tbAdded(personTb);
                }
            }
            if (CollectionUtils.isNotEmpty(tBToUpdate)) {
                personTbDaoImpl.updateBatch(tBToUpdate);
                for (PersonTb personTb : tBToUpdate) {
                    changeLogBuilder.tbUpdated(personTb);
                }
            }
            if (CollectionUtils.isNotEmpty(tbToDelete)) {
                personTbDaoImpl.deleteByIds(getIds(tbToDelete));
                for (PersonTb personTb : tbToDelete) {
                    changeLogBuilder.tbDeleted(personTb);
                }
            }
        }



        personToPersist.setId(personDTO.getId());
        personToPersist.setOldId(personDTO.getOldId());
        personToPersist.setStartDate(SimpleDateUtils.toStartOfDay(personDTO.getStartDate()));
        personToPersist.setEndDate(SimpleDateUtils.toStartOfDay(personDTO.getEndDate()));
        personToPersist.setLastName(personDTO.getLastName());
        personToPersist.setFirstName(personDTO.getFirstName());
        personToPersist.setMiddleName(personDTO.getMiddleName());
        personToPersist.setBirthDate(SimpleDateUtils.toStartOfDay(personDTO.getBirthDate()));
        // Тут и ниже устанавливаем пустые объекты вместо null, т.к. сохранение работает через BeanPropertySqlParameterSource, и оно иначе не будет работать
        personToPersist.setCitizenship(personDTO.getCitizenship().value() != null ? personDTO.getCitizenship().value() : new RefBookCountry());
        personToPersist.setSource(personDTO.getSource() != null ? personDTO.getSource() : new RefBookAsnu());
        if (personDTO.getReportDoc().value() != null) {
            IdDoc idDoc = personDTO.getReportDoc().value();
            if (idDoc.getId() == null) {
                persistedIdDocs = idDocDaoImpl.getByPerson(personToPersist);
            }
            for (IdDoc persistedIdDoc : persistedIdDocs) {
                if (persistedIdDoc.getDocumentNumber().equals(idDoc.getDocumentNumber()) && persistedIdDoc.getDocType().getCode().equals(idDoc.getDocType().getCode())) {
                    idDoc.setId(persistedIdDoc.getId());
                }
            }
            personToPersist.setReportDoc(idDoc);
        } else {
            personToPersist.setReportDoc(new IdDoc());
        }
        personToPersist.setInn(personDTO.getInn().value());
        personToPersist.setInnForeign(personDTO.getInnForeign().value());
        personToPersist.setSnils(personDTO.getSnils().value());
        personToPersist.setTaxPayerState(personDTO.getTaxPayerState().value() != null ? personDTO.getTaxPayerState().value() : new RefBookTaxpayerState());
        personToPersist.setVip(personDTO.isVip());
        personToPersist.getAddress().setRegionCode(personDTO.getAddress().value().getRegionCode());
        personToPersist.getAddress().setPostalCode(personDTO.getAddress().value().getPostalCode());
        personToPersist.getAddress().setDistrict(personDTO.getAddress().value().getDistrict());
        personToPersist.getAddress().setCity(personDTO.getAddress().value().getCity());
        personToPersist.getAddress().setLocality(personDTO.getAddress().value().getLocality());
        personToPersist.getAddress().setStreet(personDTO.getAddress().value().getStreet());
        personToPersist.getAddress().setHouse(personDTO.getAddress().value().getHouse());
        personToPersist.getAddress().setBuild(personDTO.getAddress().value().getBuild());
        personToPersist.getAddress().setAppartment(personDTO.getAddress().value().getAppartment());
        personToPersist.getAddress().setCountry(personDTO.getAddress().value().getCountry() != null ? personDTO.getAddress().value().getCountry() : new RefBookCountry());
        personToPersist.getAddress().setAddressIno(personDTO.getAddress().value().getAddressIno());
        changeLogBuilder.personInfoUpdated(persistedPerson, personToPersist);

        refBookPersonDao.updateRegistryPerson(personToPersist);
        String note = changeLogBuilder.build();
        if (isNotEmpty(note)) {
            logBusinessService.logPersonEvent(personToPersist.getId(), FormDataEvent.UPDATE_PERSON, note, userInfo);
        }
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
        idDocDaoImpl.createBatch(idDocToSave);
        idDocDaoImpl.updateBatch(idDocToUpdate);
        idTaxPayerDaoImpl.createBatch(idTaxPayers);
        personTbDaoImpl.createBatch(personTbs);

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
            } else {
                result.setFormattedNumber(docNumber);
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
    @Transactional(readOnly = true)
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
        idDocDaoImpl.createBatch(idDocs);
        idTaxPayerDaoImpl.createBatch(idTaxPayers);
        personTbDaoImpl.createBatch(personTbs);

        refBookPersonDao.updateBatch(personList);

        return personList;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistryPerson> findActualRefPersonsByDeclarationDataId(Long declarationDataId) {
        List<RegistryPerson> result = refBookPersonDao.findActualRefPersonsByDeclarationDataId(declarationDataId, new Date());
        for (RegistryPerson person : result) {
            person.getPersonIdentityList().addAll(idTaxPayerDaoImpl.getByPerson(person));
            person.getDocuments().addAll(idDocDaoImpl.getByPerson(person));
        }
        return result;
    }

    private RegistryPerson findOriginal(long id) {
        List<RegistryPerson> versions = refBookPersonDao.findAllOriginalVersions(id);
        return resolveVersion(versions, new Date());
    }

    private List<RegistryPerson> findAllDuplicates(long id) {
        List<RegistryPerson> candidates = refBookPersonDao.findAllDuplicatesVersions(id);
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
        return pickedDuplicates;
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

    private static List<Long> getOldIds(Collection<RegistryPerson> persons) {
        List<Long> ids = new ArrayList<>();
        for (RegistryPerson person : persons) {
            ids.add(person.getOldId());
        }
        return ids;
    }

    private static void removeByOldId(Collection<RegistryPerson> persons, long recordId) {
        for (Iterator<RegistryPerson> iterator = persons.iterator(); iterator.hasNext(); ) {
            RegistryPerson person = iterator.next();
            if (recordId == person.getOldId()) {
                iterator.remove();
                return;
            }
        }
    }

    private static RegistryPerson findByOldId(Collection<RegistryPerson> persons, long recordId) {
        for (RegistryPerson person : persons) {
            if (recordId == person.getOldId()) {
                return person;
            }
        }
        return null;
    }
}
