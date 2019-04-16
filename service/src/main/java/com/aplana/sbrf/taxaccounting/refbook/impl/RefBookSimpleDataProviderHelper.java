package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.dao.impl.refbook.RefBookSimpleDaoImpl;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.RefBookUtils;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.util.DBUtils;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.refbook.RefBookHelper;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import com.aplana.sbrf.taxaccounting.utils.SimpleDateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

@Component
@Transactional
public class RefBookSimpleDataProviderHelper {

    private static final String UNIQUE_ERROR_MSG = "Нарушено требование к уникальности, уже существуют записи %s в указанном периоде действия записи! Проверяемая запись справочника: %s";
    private static final String CROSS_ERROR_MSG = "Обнаружено пересечение указанного срока актуальности с существующей версией!";

    private static final ThreadLocal<SimpleDateFormat> formatter = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };

    @Autowired
    private CommonRefBookService commonRefBookService;
    @Autowired
    private RefBookFactory refBookFactory;
    @Autowired
    private RefBookHelper refBookHelper;
    @Autowired
    private LogEntryService logEntryService;
    @Autowired
    private DBUtils dbUtils;
    @Autowired
    private RefBookDao refBookDao;
    @Autowired
    private RefBookSimpleDaoImpl dao;

    /**
     * Проверка корректности
     */
    void checkCorrectness(Logger logger, RefBook refBook, Long uniqueRecordId, Date versionFrom, List<RefBookRecord> records) {
        List<RefBookAttribute> attributes = refBook.getAttributes();
        //Проверка обязательности заполнения записей справочника
        List<String> errors = RefBookUtils.checkFillRequiredRefBookAtributes(attributes, records);
        if (!errors.isEmpty()) {
            throw new ServiceException("Поля " + errors.toString() + " являются обязательными для заполнения");
        }

        //Проверка корректности значений атрибутов
        errors = RefBookUtils.checkRefBookAtributeValues(attributes, records);
        if (!errors.isEmpty()) {
            for (String error : errors) {
                logger.error(error);
            }
            throw new ServiceException("Обнаружено некорректное значение атрибута");
        }

        // Проверяем каждую запись по-отдельности на уникальность значений
        for (RefBookRecord record : records) {
            // Найденные совпадения [uniqRecordId-атрибут(ы), ...]
            List<Pair<Long, String>> matchedRecords = dao.getMatchedRecordsByUniqueAttributes(refBook, uniqueRecordId, record);
            if (matchedRecords != null && !matchedRecords.isEmpty()) {
                List<Long> conflictedIds = dao.checkConflictValuesVersions(refBook, matchedRecords, versionFrom, record.getVersionTo());
                // Если значения совпадают и перересекаются сроки действия записей, то выбрасываем ошибку
                if (!conflictedIds.isEmpty()) {
                    throw new ServiceException(makeAttrNames(refBook, record, matchedRecords, conflictedIds));
                }
            }
        }

        if (refBook.isVersioned()) {
            //Проверка ссылочных значений
            checkReferences(refBook, attributes, records, versionFrom, logger);
        }
    }

    /**
     * Формирует строку с информацией о конфликтующих значениях
     *
     * @param refBook
     * @param record
     * @param matchedRecords
     * @param conflictedIds
     * @return
     */
    private String makeAttrNames(RefBook refBook, RefBookRecord record, List<Pair<Long, String>> matchedRecords, List<Long> conflictedIds) {
        // [алиас атрибута : количество дублей] дублей может быть > 1, так как текущая запись может пересекать несколько интервалов времени
        Map<String, Integer> map = new HashMap<>();
        if (conflictedIds != null) {
            //Если было ограничение по периоду, то отбираем нужные
            for (Long id : conflictedIds) {
                for (Pair<Long, String> pair : matchedRecords) {
                    if (pair.getFirst().equals(id)) {
                        Integer count = map.get(pair.getSecond());
                        if (count == null) count = 0;
                        count++;
                        map.put(pair.getSecond(), count);
                    }
                }
            }
        } else {
            //Иначе просто все совпадения
            for (Pair<Long, String> pair : matchedRecords) {
                Integer count = map.get(pair.getSecond());
                if (count == null) count = 0;
                count++;
                map.put(pair.getSecond(), count);
            }
        }

        StringBuilder attrNames = new StringBuilder();
        Iterator<Map.Entry<String, Integer>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> pair = iterator.next();
            attrNames
                    .append("(")
                    .append(pair.getValue())
                    .append(" шт.) со значениями атрибута(ов) \"")
                    .append(pair.getKey())
                    .append("\"");
            if (iterator.hasNext()) {
                attrNames.append(", ");
            }
        }

        // Строка с информацией о проверяемой строке - значения атрибутов в виде строки
        String strRecord = refBookHelper.refBookRecordToString(refBook, record);
        return String.format(UNIQUE_ERROR_MSG, attrNames.toString(), strRecord);
    }

    private void checkReferences(RefBook refBook, List<RefBookAttribute> attributes, List<RefBookRecord> records, Date versionFrom, Logger logger) {
        if (!attributes.isEmpty()) {
            Map<String, RefBookDataProvider> providers = new HashMap<>();
            Map<RefBookDataProvider, List<RefBookLinkModel>> references = new HashMap<>();
            List<String> uniqueAliases = new ArrayList<>();

            for (RefBookAttribute attribute : attributes) {
                if (attribute.getUnique() != 0) {
                    uniqueAliases.add(attribute.getAlias());
                }
            }
            Integer i = null;
            //Группируем ссылки по провайдерам
            for (RefBookRecord record : records) {
                Map<String, RefBookValue> values = record.getValues();
                for (RefBookAttribute attribute : attributes) {
                    if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE) &&
                            values.get(attribute.getAlias()) != null && !values.get(attribute.getAlias()).isEmpty() &&
                            !attribute.getAlias().equals("DEPARTMENT_ID")) {       //Подразделения не версионируются и их нет смысла проверять
                        Long id = values.get(attribute.getAlias()).getReferenceValue();

                        RefBook attributeRefBook = commonRefBookService.getByAttribute(attribute.getRefBookAttributeId());
                        RefBookDataProvider provider;
                        if (!providers.containsKey(attributeRefBook.getTableName())) {
                            provider = refBookFactory.getDataProvider(attributeRefBook.getId());
                            providers.put(attributeRefBook.getTableName(), provider);
                        } else {
                            provider = providers.get(attributeRefBook.getTableName());
                        }
                        if (!references.containsKey(provider)) {
                            references.put(provider, new ArrayList<RefBookLinkModel>());
                        }
                        StringBuilder specialId = null;
                        if (records.size() > 1) {
                            //Если обрабатывается несколько записей, то просто названия поля не хватит. Формируем имя записи из уникальных аттрибутов, а если их нет, то из строковых
                            specialId = new StringBuilder();
                            if (uniqueAliases.size() > 0) {
                                for (Iterator<String> it = uniqueAliases.iterator(); it.hasNext(); ) {
                                    String uniqueAlias = it.next();
                                    specialId.append(values.get(uniqueAlias));
                                    if (it.hasNext()) {
                                        specialId.append("/");
                                    }
                                }
                            } else {
                                for (Iterator<Map.Entry<String, RefBookValue>> it = values.entrySet().iterator(); it.hasNext(); ) {
                                    Map.Entry<String, RefBookValue> value = it.next();
                                    if (value.getValue().getAttributeType() == RefBookAttributeType.STRING) {
                                        specialId.append(values.get(value.getValue().getStringValue()));
                                        if (it.hasNext()) {
                                            specialId.append("/");
                                        }
                                    }
                                }
                            }
                        }
                        references.get(provider).add(new RefBookLinkModel(i, attribute.getAlias(), id, specialId != null ? specialId.toString() : null, versionFrom, record.getVersionTo()));
                    }
                }
            }
            refBookHelper.checkReferenceValues(refBook, references, logger);
        }
    }

    /**
     * Обработка пересечений версий
     *
     * @return нужна ли дальнейшая обработка даты окончания (фиктивной версии)? Она могла быть выполнена в процессе проверки пересечения
     */
    boolean crossVersionsProcessing(List<CheckCrossVersionsResult> results, RefBook refBook, Date versionFrom, Date versionTo, Logger logger) {
        for (CheckCrossVersionsResult result : results) {
            if (result.getResult() == CrossResult.FATAL_ERROR) {
                throw new ServiceException(CROSS_ERROR_MSG);
            }
        }

        for (CheckCrossVersionsResult result : results) {
            if (result.getResult() == CrossResult.NEED_CHECK_USAGES) {
                //Ищем все ссылки на запись справочника в новом периоде
                //Deprecated: checkUsages(refBook, Arrays.asList(result.getRecordId()), versionFrom, versionTo, true, logger, CROSS_ERROR_MSG);
                if (logger != null) {
                    logger.info("Установлена дата окончания актуальности версии " + formatter.get().format(SimpleDateUtils.addDayToDate(versionFrom, -1)) + " для предыдущей версии");
                }
            }
            if (result.getResult() == CrossResult.NEED_CHANGE) {
                refBookDao.updateVersionRelevancePeriod(refBook.getTableName(), result.getRecordId(), SimpleDateUtils.addDayToDate(versionTo, 1));
                return false;
            }
            if (result.getResult() == CrossResult.NEED_DELETE) {
                refBookDao.deleteRecordVersions(refBook.getTableName(), Arrays.asList(result.getRecordId()), false);
            }
        }
        return true;
    }

    /**
     * Сохраняет записи справочника в базу.
     *
     * @param refBook                   объект справочника
     * @param versionFrom               дата начала версии
     * @param versionTo                 дата окончания версии
     * @param records                   сохраняемые записи
     * @param countIds                  количество ID записей, которые нужно создать
     * @param excludedVersionEndRecords список из ID записей, которым не нужно проставлять дату окончания версии
     * @param logger                    логгер
     * @return список ID сохраненных записей
     */
    List<Long> createVersions(RefBook refBook, Date versionFrom, Date versionTo, List<RefBookRecord> records, int countIds, List<Long> excludedVersionEndRecords, Logger logger) {
        //Генерим record_id для новых записей. Нужно для связи настоящей и фиктивной версий
        List<Long> generatedIds = dbUtils.getNextIds(DBUtils.Sequence.REF_BOOK_RECORD_ROW, countIds);
        if (refBook.isVersioned()) {
            int counter = 0;
            for (RefBookRecord record : records) {
                RefBookRecordVersion nextVersion = null;
                if (record.getRecordId() != null) {
                    nextVersion = dao.getNextVersion(refBook, record.getRecordId(), versionFrom);
                } else {
                    record.setRecordId(generatedIds.get(counter));
                    counter++;
                }
                if (versionTo == null) {
                    if (nextVersion != null && logger != null) {
                        logger.infoIfNotExist("Установлена дата окончания актуальности версии " +
                                formatter.get().format(SimpleDateUtils.addDayToDate(nextVersion.getVersionStart(), -1)) +
                                " в связи с наличием следующей версии");
                    }
                } else {
                    if (!excludedVersionEndRecords.contains(record.getRecordId())) {
                        if (nextVersion == null) {
                            //Следующая версия не существует - создаем фиктивную версию
                            dao.createFakeRecordVersion(refBook, record.getRecordId(), SimpleDateUtils.addDayToDate(versionTo, 1));
                        } else {
                            int days = SimpleDateUtils.daysBetween(versionTo, nextVersion.getVersionStart());
                            if (days != 1) {
                                dao.createFakeRecordVersion(refBook, record.getRecordId(), SimpleDateUtils.addDayToDate(versionTo, 1));
                            }
                        }
                    }
                }
            }
        } else {
            //Для каждой записи своя группа, т.к версий нет
            int counter = 0;
            for (RefBookRecord record : records) {
                record.setRecordId(generatedIds.get(counter));
                counter++;
            }
        }

        return dao.createRecordVersion(refBook, versionFrom, VersionedObjectStatus.NORMAL, records);
    }
}
