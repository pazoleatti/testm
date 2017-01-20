package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.dao.impl.refbook.RefBookUtils;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.FormLink;
import com.aplana.sbrf.taxaccounting.model.TaskInterruptCause;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.refbook.RefBookHelper;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.util.BDUtils;
import com.aplana.sbrf.taxaccounting.utils.SimpleDateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

@Component
@Transactional
public class RefBookSimpleDataProviderHelper {
    private static final String UNIQ_ERROR_MSG = "Нарушено требование к уникальности, уже существуют записи %s в указанном периоде!";
    private static final String CROSS_ERROR_MSG = "Обнаружено пересечение указанного срока актуальности с существующей версией!";

    private static final ThreadLocal<SimpleDateFormat> formatter = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };

    @Autowired
    private RefBookSimpleDataProvider provider;
    @Autowired
    private RefBookFactory rbFactory;
    @Autowired
    private RefBookHelper refBookHelper;
    @Autowired
    private FormDataService formDataService;
    @Autowired
    private LogEntryService logEntryService;
    @Autowired
    private BDUtils dbUtils;
    @Autowired
    private RefBookDao refBookDao;

    /**
     * Проверка корректности
     */
    // TODO использую в скрипте
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

        //Признак настроек подразделений
        boolean isConfig = refBook.getId().equals(RefBook.DEPARTMENT_CONFIG_TRANSPORT) ||
                refBook.getId().equals(RefBook.DEPARTMENT_CONFIG_INCOME) ||
                refBook.getId().equals(RefBook.DEPARTMENT_CONFIG_DEAL) ||
                refBook.getId().equals(RefBook.DEPARTMENT_CONFIG_VAT) ||
                refBook.getId().equals(RefBook.DEPARTMENT_CONFIG_PROPERTY) ||
                refBook.getId().equals(RefBook.DEPARTMENT_CONFIG_LAND) ||
                refBook.getId().equals(RefBook.WithTable.PROPERTY.getTableRefBookId()) ||
                refBook.getId().equals(RefBook.WithTable.TRANSPORT.getTableRefBookId()) ||
                refBook.getId().equals(RefBook.WithTable.INCOME.getTableRefBookId()) ||
                refBook.getId().equals(RefBook.WithTable.LAND.getTableRefBookId());

        if (!isConfig) {

            //Проверка отсутствия конфликта с датой актуальности родительского элемента
            if (refBook.isHierarchic() && refBook.isVersioned()) {
                checkParentConflict(logger, versionFrom, records);
            }

            for (RefBookRecord record : records) {
                //Получаем записи у которых совпали значения уникальных атрибутов
                List<Pair<Long, String>> matchedRecords = refBookDao.getMatchedRecordsByUniqueAttributes(refBook.getId(), uniqueRecordId, attributes, Arrays.asList(record));
                if (matchedRecords != null && !matchedRecords.isEmpty()) {
                    //Проверка на пересечение версий у записей справочника, в которых совпали уникальные атрибуты
                    List<Long> conflictedIds = refBookDao.checkConflictValuesVersions(matchedRecords, versionFrom, record.getVersionTo());

                    if (!conflictedIds.isEmpty()) {
                        throw new ServiceException(String.format(UNIQ_ERROR_MSG, makeAttrNames(matchedRecords, conflictedIds)));
                    }
                }
            }
        }

        if (refBook.isVersioned()) {
            //Проверка ссылочных значений
            checkReferences(refBook, attributes, records, versionFrom, logger);
        }
    }

    /**
     * Проверка отсутствия конфликта с датой актуальности родительского элемента
     */
    private void checkParentConflict(Logger logger, Date versionFrom, List<RefBookRecord> records) {
        List<Pair<Long, Integer>> checkResult = refBookDao.checkParentConflict(versionFrom, records);
        if (!checkResult.isEmpty()) {
            for (Pair<Long, Integer> conflict : checkResult) {
                //Дата окончания периода актуальности проверяемой версии больше даты окончания периода актуальности родительской записи для проверяемой версии
                if (conflict.getSecond() == 1) {
                    logger.error("Запись " + findNameByParent(records, conflict.getFirst()) +
                            ": Дата окончания периода актуальности версии должна быть не больше даты окончания периода актуальности записи, которая является родительской в иерархии!");
                }
                //Дата начала периода актуальности проверяемой версии меньше даты начала периода актуальности родительской записи для проверяемой версии
                if (conflict.getSecond() == -1) {
                    logger.error("Запись " + findNameByParent(records, conflict.getFirst()) +
                            ": Дата начала периода актуальности версии должна быть не меньше даты начала периода актуальности записи, которая является родительской в иерархии!");
                }
            }
            if (logger.containsLevel(LogLevel.ERROR)) {
                throw new ServiceLoggerException("Запись не сохранена. Обнаружены фатальные ошибки!", logEntryService.save(logger.getEntries()));
            }
        }
    }

    private String findNameByParent(List<RefBookRecord> records, Long parentId) {
        for (RefBookRecord record : records) {
            if (record.getValues().get(RefBook.RECORD_PARENT_ID_ALIAS).getReferenceValue().equals(parentId)) {
                return record.getValues().get("NAME").getStringValue();
            }
        }
        throw new ServiceException("Не найдена запись с заданным родительским элементом");
    }

    private String makeAttrNames(List<Pair<Long, String>> matchedRecords, List<Long> conflictedIds) {
        StringBuilder attrNames = new StringBuilder();
        Map<String, Integer> map = new HashMap<String, Integer>();
        if (conflictedIds != null) {
            //Если было ограничение по периоду то отираем нужные
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

        Iterator<Map.Entry<String, Integer>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> pair = iterator.next();
            attrNames
                    .append("(")
                    .append(pair.getValue())
                    .append(" шт.) с такими значениями атрибута \"")
                    .append(pair.getKey())
                    .append("\"");
            if (iterator.hasNext()) {
                attrNames.append(", ");
            }
        }
        return attrNames.toString();
    }

    private void checkReferences(RefBook refBook, List<RefBookAttribute> attributes, List<RefBookRecord> records, Date versionFrom, Logger logger) {
        if (!attributes.isEmpty()) {
            Map<String, RefBookDataProvider> providers = new HashMap<String, RefBookDataProvider>();
            Map<RefBookDataProvider, List<RefBookLinkModel>> references = new HashMap<RefBookDataProvider, List<RefBookLinkModel>>();
            List<String> uniqueAliases = new ArrayList<String>();

            for (RefBookAttribute attribute : attributes) {
                if (attribute.getUnique() != 0) {
                    uniqueAliases.add(attribute.getAlias());
                }
            }
            boolean isDepartmentConfigTable = false;
            Integer i = null;
            if (Arrays.asList(RefBook.WithTable.INCOME.getTableRefBookId(),
                    RefBook.WithTable.PROPERTY.getTableRefBookId(),
                    RefBook.WithTable.TRANSPORT.getTableRefBookId()).contains(refBook.getId())) {
                isDepartmentConfigTable = true;
                i = 1;
            }
            //Группируем ссылки по провайдерам
            for (RefBookRecord record : records) {
                Map<String, RefBookValue> values = record.getValues();
                for (RefBookAttribute attribute : attributes) {
                    if (attribute.getAttributeType().equals(RefBookAttributeType.REFERENCE) &&
                            values.get(attribute.getAlias()) != null && !values.get(attribute.getAlias()).isEmpty() &&
                            !attribute.getAlias().equals("DEPARTMENT_ID")) {       //Подразделения не версионируются и их нет смысла проверять
                        Long id = values.get(attribute.getAlias()).getReferenceValue();

                        RefBook attributeRefBook = rbFactory.getByAttribute(attribute.getRefBookAttributeId());
                        RefBookDataProvider provider;
                        if (!providers.containsKey(attributeRefBook.getTableName())) {
                            provider = rbFactory.getDataProvider(attributeRefBook.getId());
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
                if (isDepartmentConfigTable) i++;
            }
            if (Arrays.asList(RefBook.WithTable.INCOME.getRefBookId(), RefBook.WithTable.INCOME.getTableRefBookId(),
                    RefBook.WithTable.PROPERTY.getRefBookId(), RefBook.WithTable.PROPERTY.getTableRefBookId(),
                    RefBook.WithTable.TRANSPORT.getRefBookId(), RefBook.WithTable.TRANSPORT.getTableRefBookId(),
                    RefBook.DEPARTMENT_CONFIG_DEAL, RefBook.DEPARTMENT_CONFIG_VAT).contains(refBook.getId())) {
                refBookHelper.checkReferenceValues(refBook, references, RefBookHelper.CHECK_REFERENCES_MODE.DEPARTMENT_CONFIG, logger);
            } else {
                refBookHelper.checkReferenceValues(refBook, references, RefBookHelper.CHECK_REFERENCES_MODE.REFBOOK, logger);
            }
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
                if (refBook.isHierarchic()) {
                    //Поиск среди дочерних элементов
                    List<Pair<Date, Date>> childrenVersions = refBookDao.isVersionUsedLikeParent(refBook.getId(), result.getRecordId(), versionFrom);
                    if (childrenVersions != null && !childrenVersions.isEmpty()) {
                        for (Pair<Date, Date> versions : childrenVersions) {
                            if (logger != null) {
                                logger.error(String.format("Существует дочерняя запись, действует с %s по %s",
                                        formatter.get().format(versions.getFirst()), versions.getSecond() != null ? formatter.get().format(versions.getSecond()) : "-"));
                            }
                        }
                        throw new ServiceException(CROSS_ERROR_MSG);
                    }
                }

                //Ищем все ссылки на запись справочника в новом периоде
                checkUsages(refBook, Arrays.asList(result.getRecordId()), versionFrom, versionTo, true, logger, CROSS_ERROR_MSG);
                if (logger != null) {
                    logger.info("Установлена дата окончания актуальности версии " + formatter.get().format(SimpleDateUtils.addDayToDate(versionFrom, -1)) + " для предыдущей версии");
                }
            }
            if (result.getResult() == CrossResult.NEED_CHANGE) {
                refBookDao.updateVersionRelevancePeriod(RefBook.REF_BOOK_RECORD_TABLE_NAME, result.getRecordId(), SimpleDateUtils.addDayToDate(versionTo, 1));
                return false;
            }
            if (result.getResult() == CrossResult.NEED_DELETE) {
                refBookDao.deleteRecordVersions(RefBook.REF_BOOK_RECORD_TABLE_NAME, Arrays.asList(result.getRecordId()), false);
            }
        }
        return true;
    }

    private void checkUsages(RefBook refBook, List<Long> uniqueRecordIds, Date versionFrom, Date versionTo, Boolean restrictPeriod, Logger logger, String errorMsg) {
        //Проверка использования
        if (refBook.isHierarchic()) {
            //Поиск среди дочерних элементов
            for (Long uniqueRecordId : uniqueRecordIds) {
                List<Pair<Date, Date>> childrenVersions = refBookDao.isVersionUsedLikeParent(refBook.getId(), uniqueRecordId, versionFrom);
                if (childrenVersions != null && !childrenVersions.isEmpty()) {
                    for (Pair<Date, Date> versions : childrenVersions) {
                        if (logger != null) {
                            String msg = "Существует дочерняя запись";
                            if (refBook.isVersioned()) {
                                msg = msg + ", действует с " + formatter.get().format(versions.getFirst()) +
                                        (versions.getSecond() != null ? " по " + formatter.get().format(versions.getSecond()) : "-");
                            }
                            logger.error(msg);
                        }
                    }
                    throw new ServiceException(errorMsg);
                }
            }
        }

        boolean used = false;

        //Проверка использования в справочниках
        List<String> refBooks = refBookDao.isVersionUsedInRefBooks(refBook.getId(), uniqueRecordIds, versionFrom, versionTo, restrictPeriod,
                RefBook.WithTable.getTablesIdByRefBook(refBook.getId()) != null ?
                        Arrays.asList(RefBook.WithTable.getTablesIdByRefBook(refBook.getId())) : null);
        for (String refBookMsg : refBooks) {
            logger.error(refBookMsg);
            used = true;
        }

        //Проверка использования в нф
        List<FormLink> forms = provider.isVersionUsedInForms(refBook.getId(), uniqueRecordIds, versionFrom, versionTo, restrictPeriod);
        for (FormLink form : forms) {
            //Исключаем экземпляры в статусе "Создана" использующих справочник "Участники ТЦО"
            if (refBook.getId() == RefBook.TCO && form.getState() == WorkflowState.CREATED) {
                //Для нф в статусе "Создана" удаляем сформированные печатные представления, отменяем задачи на их формирование и рассылаем уведомления
                formDataService.deleteReport(form.getFormDataId(), false, logger.getTaUserInfo(),
                        TaskInterruptCause.REFBOOK_RECORD_MODIFY.setArgs(refBook.getName()));
                /*
                reportService.delete(form.getFormDataId(), null);
                List<ReportType> interruptedReportTypes = Arrays.asList(ReportType.EXCEL, ReportType.CSV);
                for (ReportType interruptedType : interruptedReportTypes) {
                    List<String> taskKeyList = new ArrayList<String>();
                    if (ReportType.CSV.equals(interruptedType) || ReportType.EXCEL.equals(interruptedType)) {
                        taskKeyList.addAll(formDataService.generateReportKeys(interruptedType, form.getFormDataId(), null));
                    } else {
                        taskKeyList.add(formDataService.generateTaskKey(form.getFormDataId(), interruptedType));
                    }
                    for(String key: taskKeyList) {
                        LockData lockData = lockService.getLock(key);
                        if (lockData != null) {
                            lockService.interruptTask(lockData, logger.getTaUserInfo().getUser().getId(), true, cause);
                        }
                    }
                }*/
            } else {
                logger.error(form.getMsg());
                used = true;
            }
        }

        //Проверка использования в настройках подразделений
        List<String> configs = refBookDao.isVersionUsedInDepartmentConfigs(refBook.getId(), uniqueRecordIds, versionFrom, versionTo, restrictPeriod,
                RefBook.WithTable.getTablesIdByRefBook(refBook.getId()) != null ?
                        Arrays.asList(RefBook.WithTable.getTablesIdByRefBook(refBook.getId())) : null);
        for (String configMsg : configs) {
            logger.error(configMsg);
            used = true;
        }

        if (used) {
            throw new ServiceException(errorMsg);
        }
    }

    // TODO использую в скрипте
    List<Long> createVersions(RefBook refBook, Date versionFrom, Date versionTo, List<RefBookRecord> records, long countIds, List<Long> excludedVersionEndRecords, Logger logger) {
        //Генерим record_id для новых записей. Нужно для связи настоящей и фиктивной версий
        List<Long> generatedIds = dbUtils.getNextIds(BDUtils.Sequence.REF_BOOK_RECORD_ROW, countIds);
        if (refBook.isVersioned()) {
            int counter = 0;
            for (RefBookRecord record : records) {
                RefBookRecordVersion nextVersion = null;
                if (record.getRecordId() != null) {
                    nextVersion = refBookDao.getNextVersion(refBook.getId(), record.getRecordId(), versionFrom);
                } else {
                    record.setRecordId(generatedIds.get(counter));
                    counter++;
                }
                if (versionTo == null) {
                    if (nextVersion != null && logger != null) {
                        logger.infoIfNotExist("Установлена дата окончания актуальности версии " + formatter.get().format(SimpleDateUtils.addDayToDate(nextVersion.getVersionStart(), -1)) + " в связи с наличием следующей версии");
                    }
                } else {
                    if (!excludedVersionEndRecords.contains(record.getRecordId())) {
                        if (nextVersion == null) {
                            //Следующая версия не существует - создаем фиктивную версию
                            refBookDao.createFakeRecordVersion(refBook.getId(), record.getRecordId(), SimpleDateUtils.addDayToDate(versionTo, 1));
                        } else {
                            int days = SimpleDateUtils.daysBetween(versionTo, nextVersion.getVersionStart());
                            if (days != 1) {
                                refBookDao.createFakeRecordVersion(refBook.getId(), record.getRecordId(), SimpleDateUtils.addDayToDate(versionTo, 1));
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

        return refBookDao.createRecordVersion(refBook.getId(), versionFrom, VersionedObjectStatus.NORMAL, records);
    }
}
