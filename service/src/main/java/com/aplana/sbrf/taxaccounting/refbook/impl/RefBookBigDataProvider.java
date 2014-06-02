package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.dao.impl.refbook.RefBookUtils;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookBigDataDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.util.BDUtils;
import com.aplana.sbrf.taxaccounting.utils.SimpleDateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Провайдер для больших справочников, которые хранятся в отдельных таблицах
 * Например: ОКТМО (в будущем окато)
 *
 * @author dloshkarev
 */
@Service("RefBookBigDataProvider")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class RefBookBigDataProvider implements RefBookDataProvider {

	// Справочник "ОКТМО"
	public static final Long OKTMO_REF_BOOK_ID = 96L;
	public static final String OKTMO_TABLE_NAME = "REF_BOOK_OKTMO";

    /** Код справочника */
    private Long refBookId;

    /** Название таблицы для запроса данных */
    private String tableName;

    @Autowired
    RefBookBigDataDao dao;

    @Autowired
    RefBookDao rbDao;

    @Autowired
    RefBookUtils refBookUtils;

    @Autowired
    LogEntryService logEntryService;

    @Autowired
    private BDUtils dbUtils;

    private final static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        return dao.getRecords(getTableName(), refBookId, version, pagingParams, filter, sortAttribute, isSortAscending);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        return getRecords(version, pagingParams, filter, sortAttribute, true);
    }

    @Override
    public List<Long> getUniqueRecordIds(Date version, String filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Pair<Long, Long>> checkRecordExistence(Date version, String filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getChildrenRecords(Long parentRecordId, Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute){
        return dao.getChildrenRecords(getTableName(), refBookId, version, parentRecordId, pagingParams, filter, sortAttribute);
    }

    @Override
    public List<Long> getParentsHierarchy(Long uniqueRecordId) {
        return refBookUtils.getParentsHierarchy(OKTMO_TABLE_NAME, uniqueRecordId);
    }

    @Override
    public Map<String, RefBookValue> getRecordData(Long recordId) {
        return dao.getRecordData(getTableName(), refBookId, recordId);
    }

    @Override
    public Map<Long, Map<String, RefBookValue>> getRecordData(List<Long> recordIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RefBookValue getValue(Long recordId, Long attributeId) {
        RefBook refBook = rbDao.get(refBookId);
        RefBookAttribute attribute = refBook.getAttribute(attributeId);
        Map<String, RefBookValue> value = dao.getRecordData(getTableName(), refBookId, recordId);
        return value != null ? value.get(attribute.getAlias()) : null;
    }

    @Override
    public List<Date> getVersions(Date startDate, Date endDate) {
        return dao.getVersions(getTableName(), startDate, endDate);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecordVersions(Long uniqueRecordId, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        return dao.getRecordVersions(getTableName(), refBookId, uniqueRecordId, pagingParams, filter, sortAttribute, true);
    }

    @Override
    public RefBookRecordVersion getRecordVersionInfo(Long uniqueRecordId) {
        return dao.getRecordVersionInfo(getTableName(), uniqueRecordId);
    }

    @Override
    public Map<Long, Date> getRecordsVersionStart(List<Long> uniqueRecordIds) {
        return dao.getRecordsVersionStart(getTableName(), uniqueRecordIds);
    }

    @Override
    public int getRecordVersionsCount(Long uniqueRecordId) {
        return dao.getRecordVersionsCount(getTableName(), uniqueRecordId);
    }

    @Override
    public List<Long> createRecordVersion(Logger logger, Date versionFrom, Date versionTo, List<RefBookRecord> records) {
        //TODO dloshkarev: надо все перепроверять. Т.к пока эти справочники read only, то эти методы не нужны
        throw new UnsupportedOperationException();
        /*try {
            RefBook refBook = rbDao.get(refBookId);
            List<RefBookAttribute> attributes = refBook.getAttributes();
            List<Long> recordIds = new ArrayList<Long>();

            long countIds = 0;
            for (RefBookRecord record : records) {
                if (record.getRecordId() == null) {
                    countIds++;
                } else {
                    recordIds.add(record.getRecordId());
                }
            }

            List<String> errors;

            //Проверка обязательности заполнения записей справочника
            errors = refBookUtils.checkFillRequiredRefBookAtributes(attributes, records);
            if (errors.size() > 0){
                throw new ServiceException("Поля " + errors.toString() + " являются обязательными для заполнения");
            }

            //Проверка корректности значений атрибутов
            errors = refBookUtils.checkRefBookAtributeValues(attributes, records);
            if (errors.size() > 0){
                for (String error : errors) {
                    logger.error(error);
                }
                throw new ServiceException("Обнаружено некорректное значение атрибута");
            }

            //Проверка корректности
            List<Pair<Long,String>> matchedRecords = dao.getMatchedRecordsByUniqueAttributes(getTableName(), attributes, records);
            if (matchedRecords == null || matchedRecords.size() == 0) {
                //Проверка ссылочных значений
                boolean isReferencesOk = refBookUtils.isReferenceValuesCorrect(getTableName(), versionFrom, attributes, records);
                if (!isReferencesOk) {
                    logger.info("Период актуальности выбранного значения меньше периода актуальности версии");
                }
            } else {
                //Проверка на пересечение версий у записей справочника, в которых совпали уникальные атрибуты
                dao.checkConflictValuesVersions(getTableName(), matchedRecords, versionFrom, versionTo);
            }

            if (recordIds.size() > 0 && dao.isVersionsExist(getTableName(), recordIds, versionFrom)) {
                throw new ServiceException("Версия с указанной датой актуальности уже существует");
            }

            for (RefBookRecord record : records) {
                //Проверка пересечения версий
                if (record.getRecordId() != null) {
                    crossVersionsProcessing(dao.checkCrossVersions(getTableName(), record.getRecordId(), versionFrom, versionTo, null),
                            versionFrom, versionTo, logger);
                }
            }

            //Создание настоящей и фиктивной версии
            return createVersions(versionFrom, versionTo, records, countIds, logger);
        } catch (Exception e) {
            if (logger != null) {
                logger.error(e);
                throw new ServiceLoggerException("Версия не создана, обнаружены фатальные ошибки!",
                        logEntryService.save(logger.getEntries()));
            } else {
                throw new ServiceException("Версия не создана, обнаружены фатальные ошибки!");
            }
        }*/
    }

    /**
     * Обработка пересечений версий
     */
    private void crossVersionsProcessing(List<CheckCrossVersionsResult> results, Date versionFrom, Date versionTo, Logger logger) {
        boolean isFatalErrorExists = false;

        for (CheckCrossVersionsResult result : results) {
            if (result.getResult() == CrossResult.NEED_CHECK_USAGES) {
                boolean isReferenceToVersionExists = dao.isVersionUsed(getTableName(), refBookId, result.getRecordId(), versionFrom);
                if (isReferenceToVersionExists) {
                    throw new ServiceException("Обнаружено пересечение указанного срока актуальности с существующей версией!");
                } else {
                    if (logger != null) {
                        logger.info("Установлена дата окончания актуальности версии "+sdf.format(SimpleDateUtils.addDayToDate(versionFrom, -1))+" для предыдущей версии");
                    }
                }
            }
            if (result.getResult() == CrossResult.NEED_CHANGE) {
                refBookUtils.updateVersionRelevancePeriod(getTableName(), result.getRecordId(), SimpleDateUtils.addDayToDate(versionTo, 1));
                updateResults(results, result);
            }
            if (result.getResult() == CrossResult.NEED_DELETE) {
                refBookUtils.deleteVersion(getTableName(), result.getRecordId());
                updateResults(results, result);
            }
        }

        for (CheckCrossVersionsResult result : results) {
            if (result.getResult() == CrossResult.FATAL_ERROR) {
                isFatalErrorExists = true;
            }
        }

        if (isFatalErrorExists) {
            throw new ServiceException("Обнаружено пересечение указанного срока актуальности с существующей версией!");
        }
    }

    private List<Long> createVersions(Date versionFrom, Date versionTo, List<RefBookRecord> records, long countIds, Logger logger) {
        //Генерим record_id для новых записей. Нужно для связи настоящей и фиктивной версий
        List<Long> generatedIds = dbUtils.getNextIds(BDUtils.Sequence.REF_BOOK_OKTMO, countIds);

        int counter = 0;
        for (RefBookRecord record : records) {
            RefBookRecordVersion nextVersion = null;
            if (record.getRecordId() != null) {
                nextVersion = dao.getNextVersion(getTableName(), record.getRecordId(), versionFrom);
            } else {
                record.setRecordId(generatedIds.get(counter));
                counter++;
            }
            if (versionTo == null) {
                if (nextVersion != null && logger != null) {
                    logger.info("Установлена дата окончания актуальности версии "+sdf.format(SimpleDateUtils.addDayToDate(nextVersion.getVersionStart(), -1))+" в связи с наличием следующей версии");
                }
            } else {
                if (nextVersion == null) {
                    //Следующая версия не существует - создаем фиктивную версию
                    dao.createFakeRecordVersion(getTableName(), record.getRecordId(), SimpleDateUtils.addDayToDate(versionTo, 1));
                } else {
                    int days = SimpleDateUtils.daysBetween(versionTo, nextVersion.getVersionStart());
                    if (days != 1) {
                        dao.createFakeRecordVersion(getTableName(), record.getRecordId(), SimpleDateUtils.addDayToDate(versionTo, 1));
                    }
                }
            }
        }

        return dao.createRecordVersion(getTableName(), refBookId, versionFrom, VersionedObjectStatus.NORMAL, records);
    }

    /**
     * Обновляет значение результата для пересекаемых версий, конфликт которых решается изменением соседних
     * В частности возможно удаление либо изменение даты окончания версии, что устраняет часть конфликтов
     * @param results все результаты проверки пересечения
     * @param wantedResult результат, для которого было выполнено устранение конфликта
     */
    private void updateResults(List<CheckCrossVersionsResult> results, CheckCrossVersionsResult wantedResult) {
        for (CheckCrossVersionsResult result : results) {
            if (result.getNum() == wantedResult.getNum() - 1) {
                if (result.getResult() == CrossResult.FATAL_ERROR) {
                    result.setResult(CrossResult.OK);
                } else {
                    throw new ServiceException("Недопустимая ситуация. Начало редактируемой версии является фиктивным: "+result.getResult());
                }
            }
        }
    }

    @Override
    public List<Pair<RefBookAttribute, RefBookValue>> getUniqueAttributeValues(Long uniqueRecordId) {
        List<Pair<RefBookAttribute, RefBookValue>> values = new ArrayList<Pair<RefBookAttribute, RefBookValue>>();
        List<RefBookAttribute> attributes =  rbDao.getAttributes(refBookId);
        for (RefBookAttribute attribute : attributes) {
            if (attribute.isUnique()) {
                values.add(new Pair<RefBookAttribute, RefBookValue>(attribute, getValue(uniqueRecordId, attribute.getId())));
            }
        }
        return values;
    }

    @Override
    public void updateRecordVersion(Logger logger, Long uniqueRecordId, Date versionFrom, Date versionTo, Map<String, RefBookValue> values) {
        //TODO dloshkarev: надо все перепроверять. Т.к пока эти справочники read only, то эти методы не нужны
        throw new UnsupportedOperationException();
        /*try {
            boolean isJustNeedValuesUpdate = (versionFrom == null && versionTo == null);

            List<RefBookAttribute> attributes = rbDao.getAttributes(refBookId);
            //Получаем идентификатор записи справочника без учета версий
            Long recordId = dao.getRecordId(getTableName(), uniqueRecordId);

            //Проверка обязательности заполнения записей справочника
            List<String> errors= refBookUtils.checkFillRequiredRefBookAtributes(attributes, values);
            if (errors.size() > 0){
                throw new ServiceException("Поля " + errors.toString() + " являются обязательными для заполнения");
            }

            RefBookRecordVersion oldVersionPeriod = dao.getRecordVersionInfo(getTableName(), uniqueRecordId);

            boolean isRelevancePeriodChanged = false;
            if (!isJustNeedValuesUpdate) {
                isRelevancePeriodChanged = !versionFrom.equals(oldVersionPeriod.getVersionStart())
                        || (versionTo != null && !versionTo.equals(oldVersionPeriod.getVersionEnd()))
                        || (oldVersionPeriod.getVersionEnd() != null && !oldVersionPeriod.getVersionEnd().equals(versionTo));

                if (isRelevancePeriodChanged) {
                    //Проверка пересечения версий
                    crossVersionsProcessing(dao.checkCrossVersions(getTableName(), recordId, versionFrom, versionTo, uniqueRecordId),
                            versionFrom, versionTo, logger);
                }

                //Проверка использования
                boolean isReferenceToVersionExists = dao.isVersionUsed(getTableName(), refBookId, uniqueRecordId, versionFrom);
                if (isReferenceToVersionExists) {
                    throw new ServiceException("Обнаружено использование версии в других точках запроса");
                }
            }

            //Обновление периода актуальности
            if (isRelevancePeriodChanged) {
                List<Long> uniqueIdAsList = Arrays.asList(uniqueRecordId);
                //Обновляем дату начала актуальности
                refBookUtils.updateVersionRelevancePeriod(getTableName(), uniqueRecordId, versionFrom);
                //Получаем запись - окончание версии. Если = null, то версия не имеет конца
                List<Long> relatedVersions = dao.getRelatedVersions(getTableName(), uniqueIdAsList);
                if (!relatedVersions.isEmpty() && relatedVersions.size() > 1) {
                    throw new ServiceException("Обнаружено несколько фиктивных версий");
                }
                if (relatedVersions.isEmpty() && versionTo != null) {
                    //Создаем новую фиктивную версию - дату окончания
                    dao.createFakeRecordVersion(getTableName(), recordId, SimpleDateUtils.addDayToDate(versionTo, 1));
                }

                if (!relatedVersions.isEmpty() && versionTo == null) {
                    //Удаляем фиктивную запись - теперь у версии нет конца
                    refBookUtils.deleteRecordVersions(getTableName(), relatedVersions);
                }

                if (!relatedVersions.isEmpty() && versionTo != null) {
                    //Изменяем существующую фиктивную версию
                    refBookUtils.updateVersionRelevancePeriod(getTableName(), relatedVersions.get(0), versionTo);
                }
            }

            //Обновление значений атрибутов версии
            dao.updateRecordVersion(getTableName(), refBookId, uniqueRecordId, values);
        } catch (Exception e) {
            if (logger != null) {
                logger.error(e);
                throw new ServiceLoggerException("Версия не сохранена, обнаружены фатальные ошибки!",
                        logEntryService.save(logger.getEntries()));
            } else {
                throw new ServiceException("Версия не сохранена, обнаружены фатальные ошибки!");
            }
        }*/
    }

    @Override
    public void updateRecordsVersionEnd(Logger logger, Date versionEnd, List<Long> uniqueRecordIds) {
        for (Long uniqueRecordId : uniqueRecordIds) {
            List<Long> relatedVersions = dao.getRelatedVersions(getTableName(), uniqueRecordIds);
            if (!relatedVersions.isEmpty() && relatedVersions.size() > 1) {
                refBookUtils.deleteRecordVersions(getTableName(), relatedVersions);
            }
            Long recordId = dao.getRecordId(getTableName(), uniqueRecordId);
            crossVersionsProcessing(dao.checkCrossVersions(getTableName(), recordId, versionEnd, null, null),
                    versionEnd, null, logger);
            dao.createFakeRecordVersion(getTableName(), recordId, SimpleDateUtils.addDayToDate(versionEnd, 1));
        }
    }

    private void checkChildren(List<Long> uniqueRecordIds) {
        //Если есть дочерние элементы - удалять нельзя
        List<Date> parentVersions = dao.hasChildren(getTableName(), uniqueRecordIds);
        if (parentVersions != null && !parentVersions.isEmpty()) {
            StringBuilder versions = new StringBuilder();
            for (int i=0; i<parentVersions.size(); i++) {
                versions.append(sdf.format(parentVersions));
                if (i < parentVersions.size() - 1) {
                    versions.append(", ");
                }
            }
            throw new ServiceException("Удаление версии от "+ versions +" невозможно, существует дочерние элементы!");
        }
    }

    @Override
    public void deleteAllRecords(Logger logger, List<Long> uniqueRecordIds) {
        //TODO реализовать когда нужен будет импорт
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteRecordVersions(Logger logger, List<Long> uniqueRecordIds, boolean force) {
        //TODO dloshkarev: надо все перепроверять. Т.к пока эти справочники read only, то эти методы не нужны
        throw new UnsupportedOperationException();
        /*try {
            boolean isReferenceToVersionExists = dao.isVersionUsed(getTableName(), refBookId, uniqueRecordIds);
            if (isReferenceToVersionExists) {
                throw new ServiceException("Удаление невозможно, обнаружено использование элемента справочника!");
            }
            RefBook refBook = rbDao.get(refBookId);
            if (refBook.isHierarchic()) {
                checkChildren(uniqueRecordIds);
            }
            List<Long> fakeVersionIds = dao.getRelatedVersions(getTableName(), uniqueRecordIds);
            uniqueRecordIds.addAll(fakeVersionIds);
            refBookUtils.deleteRecordVersions(getTableName(), uniqueRecordIds);
        } catch (Exception e) {
            if (logger != null) {
                logger.error(e);
                throw new ServiceLoggerException("Версия элемента справочника не удалена, обнаружены фатальные ошибки!",
                        logEntryService.save(logger.getEntries()));
            } else {
                throw new ServiceException("Версия элемента справочника не удалена, обнаружены фатальные ошибки!");
            }
        }*/
    }

    @Override
    public Long getFirstRecordId(Long uniqueRecordId) {
        return dao.getFirstRecordId(getTableName(), refBookId, uniqueRecordId);
    }

    @Override
    public Long getRecordId(Long uniqueRecordId) {
        return dao.getRecordId(getTableName(), uniqueRecordId);
    }

    @Override
    public void insertRecords(Date version, List<Map<String, RefBookValue>> records) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRecords(Date version, List<Map<String, RefBookValue>> records) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteRecords(Date version, List<Long> recordIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAllRecords(Date version) {
        throw new UnsupportedOperationException();
    }

    public void setRefBookId(Long refBookId) {
        this.refBookId = refBookId;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableName() {
        if (StringUtils.isEmpty(tableName)) {
            throw new IllegalArgumentException("Field \"tableName\" must be set");
        }
        return tableName;
    }
}
