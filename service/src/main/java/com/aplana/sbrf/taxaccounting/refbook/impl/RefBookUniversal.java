package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.util.BDUtils;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.RefBookUtils;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.VersionedObjectStatus;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.utils.SimpleDateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Универсальный провайдер данных
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 11.07.13 11:32
 */
@Service("refBookUniversal")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class RefBookUniversal implements RefBookDataProvider {

	@Autowired
	private RefBookDao refBookDao;

    @Autowired
    private RefBookUtils refBookUtils;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private BDUtils dbUtils;

	protected Long refBookId;

    private static final String REF_BOOK_RECORD_TABLE_NAME = "REF_BOOK_RECORD";

    private final static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

    private final static String CROSS_ERROR_MSG = "Обнаружено пересечение указанного срока актуальности с существующей версией!";

	public void setRefBookId(Long refBookId) {
		this.refBookId = refBookId;
	}

    @Override
    public List<Date> getVersions(Date startDate, Date endDate) {
        return refBookDao.getVersions(refBookId, startDate, endDate);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecordVersions(Long recordId, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        return refBookDao.getRecordVersions(refBookId, recordId, pagingParams, filter, sortAttribute);
    }

    @Override
	public PagingResult<Map<String, RefBookValue>> getChildrenRecords(Long parentRecordId, Date version,
			PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
		return refBookDao.getChildrenRecords(refBookId, parentRecordId, version, pagingParams, filter, sortAttribute);
	}

    @Override
    public List<Long> getParentsHierarchy(Long uniqueRecordId) {
        return refBookDao.getParentsHierarchy(uniqueRecordId);
    }

    public Long getRowNum(Date version, Long recordId,
                   String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        return refBookDao.getRowNum(refBookId, version, recordId, filter, sortAttribute, isSortAscending);
    }

    @Override
	public PagingResult<Map<String, RefBookValue>> getRecords(Date version, PagingParams pagingParams,
			String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
		return refBookDao.getRecords(refBookId, version, pagingParams, filter, sortAttribute, isSortAscending);
	}

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Date version, PagingParams pagingParams,
                                                              String filter, RefBookAttribute sortAttribute) {
        return getRecords(version, pagingParams, filter, sortAttribute, true);
    }

    @Override
    public List<Long>  getUniqueRecordIds(Date version, String filter) {
        //TODO: возможно нужно точное совпадение версии
        List<Pair<Long, Long>> pairs = refBookDao.getRecordIdPairs(refBookId, version, false, filter);
        List<Long> uniqueRecordIds = new ArrayList<Long>(pairs.size());
        for (Pair<Long, Long> pair : pairs){
            uniqueRecordIds.add(pair.getFirst());
        }
        return uniqueRecordIds;
    }

    @Override
    public int getRecordsCount(Date version, String filter) {
        return refBookDao.getRecordsCount(refBookId, version, filter);
    }

    @Override
    public List<Pair<Long, Long>> checkRecordExistence(Date version, String filter) {
        return refBookDao.getRecordIdPairs(refBookId, version, true, filter);
    }

    @Override
	public Map<String, RefBookValue> getRecordData(Long recordId) {
		return refBookDao.getRecordData(refBookId, recordId);
	}

    @Override
    public Map<Long, Map<String, RefBookValue>> getRecordData(List<Long> recordIds) {
        return refBookDao.getRecordData(refBookId, recordIds);
    }

    @Override
    public RefBookValue getValue(Long recordId, Long attributeId) {
        return refBookDao.getValue(recordId, attributeId);
    }

    @Override
    public RefBookRecordVersion getRecordVersionInfo(Long uniqueRecordId) {
        return refBookDao.getRecordVersionInfo(uniqueRecordId);
    }

    @Override
    public Map<Long, Date> getRecordsVersionStart(List<Long> uniqueRecordIds) {
        return refBookDao.getRecordsVersionStart(uniqueRecordIds);
    }

    @Override
    public int getRecordVersionsCount(Long uniqueRecordId) {
        return refBookDao.getRecordVersionsCount(refBookId, uniqueRecordId);
    }

    @Override
    public List<Long> createRecordVersion(Logger logger, Date versionFrom, Date versionTo, List<RefBookRecord> records) {
        try {
            //long allTime = System.nanoTime();
            RefBook refBook = refBookDao.get(refBookId);
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

            if (!refBookId.equals(RefBook.DEPARTMENT_CONFIG_TRANSPORT) &&
                    !refBookId.equals(RefBook.DEPARTMENT_CONFIG_INCOME) &&
                    !refBookId.equals(RefBook.DEPARTMENT_CONFIG_DEAL)) {

                //Проверка корректности
                checkCorrectness(logger, refBook, null, versionFrom, versionTo, attributes, records);

                if (recordIds.size() > 0 && refBookDao.isVersionsExist(refBookId, recordIds, versionFrom)) {
                    throw new ServiceException("Версия с указанной датой актуальности уже существует");
                }

                for (RefBookRecord record : records) {
                    //Проверка пересечения версий
                    if (record.getRecordId() != null) {
                        crossVersionsProcessing(refBookDao.checkCrossVersions(refBookId, record.getRecordId(), versionFrom, versionTo, null),
                                versionFrom, versionTo, logger);
                    }
                }
            }

            //Создание настоящей и фиктивной версии
            return createVersions(versionFrom, versionTo, records, countIds, logger);
            //System.out.println("all: "+((double)(System.nanoTime()-allTime)/1000000000.0)+"s");
        } catch (Exception e) {
            if (logger != null) {
                logger.error(e);
                throw new ServiceLoggerException("Версия не сохранена. Обнаружены фатальные ошибки!",
                        logEntryService.save(logger.getEntries()));
            } else {
                throw new ServiceException("Версия не сохранена. Обнаружены фатальные ошибки!");
            }
        }

    }

    /**
     * Проверка корректности
     */
    private void checkCorrectness(Logger logger, RefBook refBook, Long uniqueRecordId, Date versionFrom, Date versionTo, List<RefBookAttribute> attributes, List<RefBookRecord> records) {
        //Проверка обязательности заполнения записей справочника
        List<String> errors= refBookUtils.checkFillRequiredRefBookAtributes(attributes, records);
        if (errors.size() > 0){
            throw new ServiceException("Поля " + errors.toString() + " являются обязательными для заполнения");
        }

        //Проверка корректности значений атрибутов
        errors = refBookUtils.checkRefBookAtributeValues(attributes, records);
        if (!errors.isEmpty()){
            for (String error : errors) {
                logger.error(error);
            }
            throw new ServiceException("Обнаружено некорректное значение атрибута");
        }

        //Проверка отсутствия конфликта с датой актуальности родительского элемента
        if (refBook.isHierarchic()) {
            checkParentConflict(logger, versionFrom, versionTo, records);
        }
        //Получаем записи у которых совпали значения уникальных атрибутов
        List<Pair<Long,String>> matchedRecords = refBookDao.getMatchedRecordsByUniqueAttributes(refBookId, uniqueRecordId, attributes, records);
        if (matchedRecords != null && !matchedRecords.isEmpty()) {
            //Проверка на пересечение версий у записей справочника, в которых совпали уникальные атрибуты
            List<Long> conflictedIds = refBookDao.checkConflictValuesVersions(matchedRecords, versionFrom, versionTo);

            if (conflictedIds.size() > 0) {
                StringBuilder attrNames = new StringBuilder();
                for (Long id : conflictedIds) {
                    for (Pair<Long,String> pair : matchedRecords) {
                        if (pair.getFirst().equals(id)) {
                            attrNames.append("\"").append(pair.getSecond()).append("\", ");
                        }
                    }
                }
                attrNames.delete(attrNames.length() - 2, attrNames.length());
                throw new ServiceException("Нарушено требование к уникальности, уже существует элемент с такими значениями атрибута "+attrNames+" в указанном периоде!");
            }
        }

        //Проверка ссылочных значений
        refBookUtils.isReferenceValuesCorrect(logger, REF_BOOK_RECORD_TABLE_NAME, versionFrom, versionTo, attributes, records);
    }

    /**
     * Проверка отсутствия конфликта с датой актуальности родительского элемента
     */
    private void checkParentConflict(Logger logger, Date versionFrom, Date versionTo, List<RefBookRecord> records) {
        List<Pair<Long, Integer>> checkResult = refBookDao.checkParentConflict(versionFrom, versionTo, records);
        if (!checkResult.isEmpty()) {
            boolean error = false;
            for (Pair<Long, Integer> conflict : checkResult) {
                //Дата окончания периода актуальности проверяемой версии больше даты окончания периода актуальности родительской записи для проверяемой версии
                if (conflict.getSecond() == 1) {
                    logger.error("Запись "+findNameByParent(records, conflict.getFirst())+
                            ": Дата окончания периода актуальности версии должна быть не больше даты окончания периода актуальности записи, которая является родительской в иерархии!");
                    error = true;
                }
                //Дата начала периода актуальности проверяемой версии меньше даты начала периода актуальности родительской записи для проверяемой версии
                if (conflict.getSecond() == -1) {
                    logger.error("Запись "+findNameByParent(records, conflict.getFirst())+
                            ": Дата начала периода актуальности версии должна быть не меньше даты начала периода актуальности записи, которая является родительской в иерархии!");
                    error = true;
                }
            }
            if (error) {
                throw new ServiceException("Обнаружен конфликт с датой актуальности родительского элемента");
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

    private List<Long> createVersions(Date versionFrom, Date versionTo, List<RefBookRecord> records, long countIds, Logger logger) {
        //Генерим record_id для новых записей. Нужно для связи настоящей и фиктивной версий
        List<Long> generatedIds = dbUtils.getNextIds(BDUtils.Sequence.REF_BOOK_RECORD_ROW, countIds);

        int counter = 0;
        for (RefBookRecord record : records) {
            RefBookRecordVersion nextVersion = null;
            if (record.getRecordId() != null) {
                nextVersion = refBookDao.getNextVersion(refBookId, record.getRecordId(), versionFrom);
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
                    refBookDao.createFakeRecordVersion(refBookId, record.getRecordId(), SimpleDateUtils.addDayToDate(versionTo, 1));
                } else {
                    int days = SimpleDateUtils.daysBetween(versionTo, nextVersion.getVersionStart());
                    if (days != 1) {
                        refBookDao.createFakeRecordVersion(refBookId, record.getRecordId(), SimpleDateUtils.addDayToDate(versionTo, 1));
                    }
                }
            }
        }

        return refBookDao.createRecordVersion(refBookId, versionFrom, VersionedObjectStatus.NORMAL, records);
    }

    /**
     * Обработка пересечений версий
     */
    private void crossVersionsProcessing(List<CheckCrossVersionsResult> results, Date versionFrom, Date versionTo, Logger logger) {
        for (CheckCrossVersionsResult result : results) {
            if (result.getResult() == CrossResult.FATAL_ERROR) {
                throw new ServiceException(CROSS_ERROR_MSG);
            }
        }

        for (CheckCrossVersionsResult result : results) {
            if (result.getResult() == CrossResult.NEED_CHECK_USAGES) {
                boolean isReferenceToVersionExists = refBookDao.isVersionUsed(refBookId, result.getRecordId(), versionFrom);
                if (isReferenceToVersionExists) {
                    throw new ServiceException(CROSS_ERROR_MSG);
                } else {
                    if (logger != null) {
                        logger.info("Установлена дата окончания актуальности версии "+sdf.format(SimpleDateUtils.addDayToDate(versionFrom, -1))+" для предыдущей версии");
                    }
                }
            }
            if (result.getResult() == CrossResult.NEED_CHANGE) {
                refBookUtils.updateVersionRelevancePeriod(REF_BOOK_RECORD_TABLE_NAME, result.getRecordId(), SimpleDateUtils.addDayToDate(versionTo, 1));
                //updateResults(results, result);
            }
            if (result.getResult() == CrossResult.NEED_DELETE) {
                refBookUtils.deleteVersion(REF_BOOK_RECORD_TABLE_NAME, result.getRecordId());
                //updateResults(results, result);
            }
        }
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
        return refBookDao.getUniqueAttributeValues(refBookId, uniqueRecordId);
    }

    @Override
    public void updateRecordVersion(Logger logger, Long uniqueRecordId, Date versionFrom, Date versionTo, Map<String, RefBookValue> records) {
        try {
            boolean isJustNeedValuesUpdate = (versionFrom == null && versionTo == null);

            RefBook refBook = refBookDao.get(refBookId);
            List<RefBookAttribute> attributes = refBook.getAttributes();
            //Получаем идентификатор записи справочника без учета версий
            Long recordId = refBookDao.getRecordId(uniqueRecordId);
            //Получаем еще неотредактированную версию
            RefBookRecordVersion oldVersionPeriod = refBookDao.getRecordVersionInfo(uniqueRecordId);

            RefBookRecord refBookRecord = new RefBookRecord();
            refBookRecord.setRecordId(uniqueRecordId);
            refBookRecord.setValues(records);

            //Проверка корректности
            checkCorrectness(logger, refBook, uniqueRecordId, versionFrom, versionTo, attributes, Arrays.asList(refBookRecord));

            if (refBook.isHierarchic()) {
                RefBookValue oldParent = refBookDao.getValue(uniqueRecordId, refBook.getAttribute(RefBook.RECORD_PARENT_ID_ALIAS).getId());
                RefBookValue newParent = records.get(RefBook.RECORD_PARENT_ID_ALIAS);
                //Проверка зацикливания
                if (!newParent.equals(oldParent) &&
                        refBookDao.hasLoops(uniqueRecordId, newParent.getReferenceValue())) {
                    //Цикл найден, формируем сообщение
                    String parentRecordName = refBookUtils.buildUniqueRecordName(refBook,
                            refBookDao.getUniqueAttributeValues(refBookId, newParent.getReferenceValue()));
                    String recordName = refBookUtils.buildUniqueRecordName(refBook,
                            refBookDao.getUniqueAttributeValues(refBookId, uniqueRecordId));
                    throw new ServiceException("Версия " + parentRecordName + " не может быть указана как родительская, т.к. входит в структуру дочерних элементов версии " + recordName);
                }
            }

            boolean isRelevancePeriodChanged = false;
            if (!isJustNeedValuesUpdate) {
                assert versionFrom != null;
                isRelevancePeriodChanged = !versionFrom.equals(oldVersionPeriod.getVersionStart())
                        || (versionTo != null && !versionTo.equals(oldVersionPeriod.getVersionEnd()))
                        || (oldVersionPeriod.getVersionEnd() != null && !oldVersionPeriod.getVersionEnd().equals(versionTo));

                if (isRelevancePeriodChanged) {
                    //Проверка пересечения версий
                    //Проверяем следующую версию после даты окочания
                    RefBookRecordVersion nextVersion = refBookDao.getNextVersion(refBookId, recordId, oldVersionPeriod.getVersionStart());
                    if (versionTo != null && nextVersion != null && versionTo.after(nextVersion.getVersionStart())) {
                        throw new ServiceException(CROSS_ERROR_MSG);
                    }
                    //Проверяем предыдущую версию до даты начала
                    RefBookRecordVersion previousVersion = refBookDao.getPreviousVersion(refBookId, recordId, oldVersionPeriod.getVersionStart());
                    if (previousVersion != null &&
                            (previousVersion.isVersionEndFake() && versionFrom.before(previousVersion.getVersionEnd())
                            || versionFrom.before(previousVersion.getVersionStart()))) {
                        throw new ServiceException(CROSS_ERROR_MSG);
                    }
                    //Выполняем стандартную проверку пересечечения
                    //crossVersionsProcessing(refBookDao.checkCrossVersions(refBookId, recordId, versionFrom, versionTo, uniqueRecordId),
                    //        versionFrom, versionTo, logger);
                }
            }

            /** Проверяем изменились ли значения атрибутов */
            boolean isValuesChanged = checkValuesChanged(uniqueRecordId, records);

            //Проверка использования
            List<String> usagesResult = refBookDao.isVersionUsed(refBookId, Arrays.asList(uniqueRecordId), versionFrom, versionTo, isValuesChanged);
            if (usagesResult != null && !usagesResult.isEmpty()) {
                for (String error: usagesResult) {
                    logger.error(error);
                }
                throw new ServiceException("Изменение невозможно, обнаружено использование элемента справочника!");
            }

            //Обновление периода актуальности
            if (isRelevancePeriodChanged) {
                List<Long> uniqueIdAsList = Arrays.asList(uniqueRecordId);
                //Обновляем дату начала актуальности
                refBookUtils.updateVersionRelevancePeriod(REF_BOOK_RECORD_TABLE_NAME, uniqueRecordId, versionFrom);
                //Получаем запись - окончание версии. Если = null, то версия не имеет конца
                List<Long> relatedVersions = refBookDao.getRelatedVersions(uniqueIdAsList);
                if (!relatedVersions.isEmpty() && relatedVersions.size() > 1) {
                    throw new ServiceException("Обнаружено несколько фиктивных версий");
                }
                if (relatedVersions.isEmpty() && versionTo != null) {
                    //Создаем новую фиктивную версию - дату окончания
                    refBookDao.createFakeRecordVersion(refBookId, recordId, SimpleDateUtils.addDayToDate(versionTo, 1));
                }

                if (!relatedVersions.isEmpty() && versionTo == null) {
                    //Удаляем фиктивную запись - теперь у версии нет конца
                    refBookUtils.deleteRecordVersions(REF_BOOK_RECORD_TABLE_NAME, relatedVersions);
                }

                if (!relatedVersions.isEmpty() && versionTo != null) {
                    //Изменяем существующую фиктивную версию
                    refBookUtils.updateVersionRelevancePeriod(REF_BOOK_RECORD_TABLE_NAME, relatedVersions.get(0), versionTo);
                }
            }

            //Обновление значений атрибутов версии
            refBookDao.updateRecordVersion(refBookId, uniqueRecordId, records);
        } catch (Exception e) {
            if (logger != null) {
                logger.error(e);
                logger.clear(LogLevel.INFO);
                throw new ServiceLoggerException("Версия не сохранена, обнаружены фатальные ошибки!",
                        logEntryService.save(logger.getEntries()));
            } else {
                throw new ServiceException("Версия не сохранена, обнаружены фатальные ошибки!");
            }
        }
    }

    private boolean checkValuesChanged(Long uniqueRecordId, Map<String,RefBookValue> records) {
        Map<String,RefBookValue> oldValues = refBookDao.getRecordData(refBookId, uniqueRecordId);
        for (Map.Entry<String, RefBookValue> newValue : records.entrySet()) {
            RefBookValue oldValue = oldValues.get(newValue.getKey());
            if (!newValue.getValue().equals(oldValue)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void updateRecordsVersionEnd(Logger logger, Date versionEnd, List<Long> uniqueRecordIds) {
        for (Long uniqueRecordId : uniqueRecordIds) {
            List<Long> relatedVersions = refBookDao.getRelatedVersions(uniqueRecordIds);
            if (!relatedVersions.isEmpty() && relatedVersions.size() > 1) {
                refBookUtils.deleteRecordVersions(REF_BOOK_RECORD_TABLE_NAME, relatedVersions);
            }
            Long recordId = refBookDao.getRecordId(uniqueRecordId);
            //Проверяем следующую версию после даты окочания
            RefBookRecordVersion oldVersionPeriod = refBookDao.getRecordVersionInfo(uniqueRecordId);
            RefBookRecordVersion nextVersion = refBookDao.getNextVersion(refBookId, recordId, oldVersionPeriod.getVersionStart());
            if (versionEnd != null && nextVersion != null && versionEnd.after(nextVersion.getVersionStart())) {
                throw new ServiceException(CROSS_ERROR_MSG);
            }
            //crossVersionsProcessing(refBookDao.checkCrossVersions(refBookId, recordId, versionEnd, null, null),
            //        versionEnd, null, logger);
            refBookDao.createFakeRecordVersion(refBookId, recordId, SimpleDateUtils.addDayToDate(versionEnd, 1));
        }
    }

    private void checkChildren(List<Long> uniqueRecordIds) {
        //Если есть дочерние элементы - удалять нельзя
        List<Date> parentVersions = refBookDao.hasChildren(refBookId, uniqueRecordIds);
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
        try {
            //Проверка использования
            List<String> usagesResult = refBookDao.isVersionUsed(refBookId, uniqueRecordIds, null, null, true);
            if (usagesResult != null && !usagesResult.isEmpty()) {
                for (String error: usagesResult) {
                    logger.error(error);
                }
                throw new ServiceException("Удаление невозможно, обнаружено использование элемента справочника!");
            }
            RefBook refBook = refBookDao.get(refBookId);
            if (refBook.isHierarchic()) {
                checkChildren(uniqueRecordIds);
            }
            refBookDao.deleteAllRecordVersions(refBookId, uniqueRecordIds);
        } catch (Exception e) {
            if (logger != null) {
                logger.error(e);
                throw new ServiceLoggerException("Элемент справочника не удален, обнаружены фатальные ошибки!",
                        logEntryService.save(logger.getEntries()));
            } else {
                throw new ServiceException("Элемент справочника не удален, обнаружены фатальные ошибки!");
            }
        }
    }

    @Override
    public void deleteRecordVersions(Logger logger, List<Long> uniqueRecordIds, boolean force) {
        try {
            //Проверка использования
            List<String> usagesResult = refBookDao.isVersionUsed(refBookId, uniqueRecordIds, null, null, true);
            if (usagesResult != null && !usagesResult.isEmpty()) {
                for (String error: usagesResult) {
                    logger.error(error);
                }
                throw new ServiceException("Удаление невозможно, обнаружено использование элемента справочника!");
            }
            RefBook refBook = refBookDao.get(refBookId);
            if (refBook.isHierarchic()) {
                checkChildren(uniqueRecordIds);
            }
            List<Long> fakeVersionIds = refBookDao.getRelatedVersions(uniqueRecordIds);
            uniqueRecordIds.addAll(fakeVersionIds);
            refBookUtils.deleteRecordVersions(REF_BOOK_RECORD_TABLE_NAME, uniqueRecordIds);
        } catch (Exception e) {
            if (logger != null) {
                logger.error(e);
                throw new ServiceLoggerException("Версия элемента справочника не удалена, обнаружены фатальные ошибки!",
                        logEntryService.save(logger.getEntries()));
            } else {
                throw new ServiceException("Версия элемента справочника не удалена, обнаружены фатальные ошибки!");
            }
        }
    }

    @Override
    public Long getFirstRecordId(Long uniqueRecordId) {
        return refBookDao.getFirstRecordId(refBookId, uniqueRecordId);
    }

    @Override
    public Long getRecordId(Long uniqueRecordId) {
        return refBookDao.getRecordId(uniqueRecordId);
    }

    @Override
    public Map<RefBookAttributePair, String> getAttributesValues(List<RefBookAttributePair> attributePairs) {
        return refBookDao.getAttributesValues(attributePairs);
    }

    @Override
	public void insertRecords(Date version, List<Map<String, RefBookValue>> records) {
        refBookDao.createRecords(refBookId, version, records);
        //createRecordVersion(Logger logger, Long recordId, Date versionFrom, Date versionTo, List<Map<String, RefBookValue>> records)
	}

	@Override
	public void updateRecords(Date version, List<Map<String, RefBookValue>> records) {
        refBookDao.updateRecords(refBookId, version, records);
        //updateRecordVersion(Logger logger, Long uniqueRecordId, Date versionFrom, Date versionTo, boolean isRelevancePeriodChanged, List<Map<String, RefBookValue>> records)
	}

    @Override
    public void deleteRecords(Date version, List<Long> recordIds) {
        refBookDao.deleteRecords(refBookId, version, recordIds);
        //deleteRecordVersions(Logger logger, List<Long> uniqueRecordIds)
    }

    @Override
    public void deleteAllRecords(Date version) {
        refBookDao.deleteAllRecords(refBookId, version);
        //deleteAllRecords(Logger logger, List<Long> uniqueRecordIds)
    }
}
