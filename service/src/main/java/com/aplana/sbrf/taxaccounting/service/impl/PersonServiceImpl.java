package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookPersonDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.result.ActionResult;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.PersonService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Сервис работы с периодами
 * <p>
 * Только этот сервис должен использоваться для работы с отчетными и налоговыми периодами
 */
@Service
@Transactional
public class PersonServiceImpl implements PersonService {

    @Autowired
    private RefBookPersonDao refBookPersonDao;
    @Autowired
    private RefBookFactory refBookFactory;
    @Autowired
    private LogEntryService logEntryService;

    private static final ThreadLocal<SimpleDateFormat> formatter = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };

    @Override
    public RefBookPerson getOriginal(Long personId) {
        return refBookPersonDao.getOriginal(personId);
    }

    @Override
    public String getPersonDocNumber(long personId) {
        return refBookPersonDao.getPersonDocNumber(personId);
    }

    @Override
    public PagingResult<RefBookPerson> getDuplicates(Long personId, PagingParams pagingParams) {
        return refBookPersonDao.getDuplicates(personId, pagingParams);
    }

    /**
     * Подготавливает введенный текст для использования в SQL-запросе
     *
     * @param searchPattern текст для поиска
     * @return обработанный текст - исключены лишние пробелы, кавычки и т.д
     */
    public String prepareForQuery(String searchPattern) {
        return com.aplana.sbrf.taxaccounting.model.util.StringUtils.cleanString(searchPattern).toLowerCase().replaceAll("\'", "\\\\\'");
    }

    @Override
    public PagingResult<RefBookPerson> getPersons(Long recordId, Date version, PagingParams pagingParams, String firstName, String lastName, String searchPattern, boolean exactSearch) {
        Long refBookId = RefBook.Id.PERSON.getId();
        RefBookAttribute sortAttribute = pagingParams != null && StringUtils.isNotEmpty(pagingParams.getProperty()) ?
                refBookFactory.getAttributeByAlias(refBookId, pagingParams.getProperty()) : null;
        PagingResult<RefBookPerson> records;
        if (recordId == null) {
            String filter = "";
            // Отдельная фильтрация по имени и фамилии - выполняем сначала, чтобы меньше результатов попало под полнотекстовый поиск
            if (StringUtils.isNotEmpty(firstName)) {
                filter += "LOWER(FIRST_NAME) = '" + prepareForQuery(firstName) + "'";
            }
            if (StringUtils.isNotEmpty(lastName)) {
                if (StringUtils.isNotEmpty(filter)) {
                    filter += " and ";
                }
                filter += "LOWER(LAST_NAME) = '" + prepareForQuery(lastName) + "'";
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
                } catch (ParseException e) {
                    // ignored
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
                        "LOWER(addressAsText) :searchPattern)")
                        .replaceAll(":searchPattern", (exactSearch ? "= '" + prepared + "'" : "like '%" + prepared + "%'"));
            }
            // Отбираем все записи справочника
            records = getPersons(version, pagingParams, filter, sortAttribute);
        } else {
            // Отбираем все версии записи правочника
            records = getPersonVersions(recordId, pagingParams);
        }
        return records;
    }

    @Override
    public PagingResult<RefBookPerson> getPersons(Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        return refBookPersonDao.getPersons(version, pagingParams, filter, sortAttribute);
    }

    @Override
    public PagingResult<RefBookPerson> getPersonVersions(Long recordId, PagingParams pagingParams) {
        return refBookPersonDao.getPersonVersions(recordId, pagingParams);
    }

    @Override
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
    public void setOriginal(List<Long> recordIds) {
        refBookPersonDao.setOriginal(recordIds);
    }

    @Override
    public void setDuplicate(List<Long> recordIds, Long originalId) {
        refBookPersonDao.setDuplicate(recordIds, originalId);
    }

    @Override
    public void changeRecordId(List<Long> recordIds, Long originalId) {
        refBookPersonDao.changeRecordId(recordIds, originalId);
    }
}
