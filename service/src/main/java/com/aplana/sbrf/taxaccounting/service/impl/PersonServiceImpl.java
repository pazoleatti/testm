package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookPersonDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.Permissive;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.PersonService;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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
        for(RegistryPerson candidate : candidates) {
            List<RegistryPerson> group = candidatesGroupedByOldId.get(candidate.getOldId());
            if (group == null) {
                group = new ArrayList<>();
                group.add(candidate);
                candidatesGroupedByOldId.put(candidate.getOldId(), group);
            } else {
                group.add(candidate);
            }
        }
        for(List<RegistryPerson> groupContent: candidatesGroupedByOldId.values()) {
            RegistryPerson person = resolveVersion(groupContent, actualDate);
            RefBook refBookIdDoc = refBookDao.get(RefBook.Id.ID_DOC.getId());
            if(person.getReportDoc().hasPermission()) {
                PagingResult<Map<String, RefBookValue>> idDocs = refBookDao.getRecordsWithVersionInfo(refBookIdDoc, null, null, "frb.id = " + person.getReportDoc().value().get("REPORT_DOC"), null, "asc");
                if (!idDocs.isEmpty()) {
                    person.setReportDoc(Permissive.of(commonRefBookService.dereference(refBookIdDoc, idDocs).get(0)));
                } else {
                    person.setReportDoc(Permissive.<Map<String, RefBookValue>>of(null));
                }
            }
            toReturnData.add(person);
        }
        return new PagingResult<>(toReturnData, toReturnData.size());
    }

    @Override
    // TODO Написать сюда @PreAuthorize
    public PagingResult<RefBookPerson> getPersons(PagingParams pagingParams) {
        return refBookPersonDao.getPersons(pagingParams);
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
        RefBook refBookIdDoc = refBookDao.get(RefBook.Id.ID_DOC.getId());
        RefBook refBookCountry = refBookDao.get(RefBook.Id.COUNTRY.getId());
        RefBook refBookTaxPayerState = refBookDao.get(RefBook.Id.TAXPAYER_STATUS.getId());
        RefBook refBookAsnu = refBookDao.get(RefBook.Id.ASNU.getId());
        RefBook refBookAddress = refBookDao.get(RefBook.Id.PERSON_ADDRESS.getId());

        if (person.getTaxPayerState().hasPermission()) {
            PagingResult<Map<String, RefBookValue>> refBookTaxPayerStates = refBookDao.getRecordsWithVersionInfo(refBookTaxPayerState, null, null, "frb.id = " + person.getTaxPayerState().value().get("TAXPAYER_STATE").getReferenceValue(), null, "asc");
            if (!refBookTaxPayerStates.isEmpty()) {
                person.setTaxPayerState(Permissive.of(commonRefBookService.dereference(refBookTaxPayerState, refBookTaxPayerStates).get(0)));
            } else {
                person.setTaxPayerState(Permissive.<Map<String, RefBookValue>>of(null));
            }
        }

        if (person.getCitizenship().hasPermission()) {
            PagingResult<Map<String, RefBookValue>> citizenships = refBookDao.getRecordsWithVersionInfo(refBookCountry, null, null, "frb.id = " + person.getCitizenship().value().get("CITIZENSHIP").getReferenceValue(), null, "asc");
            if (!citizenships.isEmpty()) {
                person.setCitizenship(Permissive.of(commonRefBookService.dereference(refBookCountry, citizenships).get(0)));
            } else {
                person.setCitizenship(Permissive.<Map<String, RefBookValue>>of(null));
            }
        }

        PagingResult<Map<String, RefBookValue>> sources = refBookDao.getRecordsWithVersionInfo(refBookAsnu, null, null, "frb.id = " + person.getSource().get("SOURCE_ID").getReferenceValue(), null, "asc");
        if (!sources.isEmpty()) {
            person.setSource(commonRefBookService.dereference(refBookAsnu, sources).get(0));
        } else {
            person.setSource(null);
        }

        if (person.getAddress().hasPermission()) {
            PagingResult<Map<String, RefBookValue>> addresses = refBookDao.getRecordsWithVersionInfo(refBookAddress, null, null, "frb.id = " + person.getAddress().value().get("ADDRESS").getReferenceValue(), null, "asc");
            if (!addresses.isEmpty()) {
                person.setAddress(Permissive.of(commonRefBookService.dereference(refBookAddress, addresses).get(0)));
            } else {
                person.setAddress(Permissive.<Map<String, RefBookValue>>of(null));
            }
        }

        if(person.getReportDoc().hasPermission()) {
            PagingResult<Map<String, RefBookValue>> idDocs = refBookDao.getRecordsWithVersionInfo(refBookIdDoc, null, null, "frb.id = " + person.getReportDoc().value().get("REPORT_DOC"), null, "asc");
            if (!idDocs.isEmpty()) {
                person.setReportDoc(Permissive.of(commonRefBookService.dereference(refBookIdDoc, idDocs).get(0)));
            } else {
                person.setReportDoc(Permissive.<Map<String, RefBookValue>>of(null));
            }
        }
        return person;
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> fetchReferencesList(Long recordId, Long refBookId, PagingParams pagingParams) {
        RefBook actualRefBook = refBookDao.get(refBookId);
        RefBook refBookPerson = refBookDao.get(RefBook.Id.PERSON.getId());
        PagingResult<Map<String, RefBookValue>> persons = refBookDao.getRecords(refBookPerson.getId(), refBookPerson.getTableName(), pagingParams, null, null, "record_id = " + recordId);
        Long[] versionIds = new Long[persons.size()];
        for (int i = 0; i < persons.size(); i++) {
            versionIds[i] = persons.get(i).get("id").getNumberValue().longValue();
        }
        PagingResult<Map<String, RefBookValue>> result = refBookDao.getRecords(refBookId, actualRefBook.getTableName(), pagingParams, null, null, "person_id in (" + StringUtils.join(versionIds, ", ") + ")");
        return commonRefBookService.dereference(actualRefBook, result);
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
     * @param descSortedRecords список версий ФЛ отсортированный по дате версии по убыванию
     * @param actualDate        актуальная дата по которой выбирается фверсия ФЛ
     * @return  Выбранная версия физлица
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
