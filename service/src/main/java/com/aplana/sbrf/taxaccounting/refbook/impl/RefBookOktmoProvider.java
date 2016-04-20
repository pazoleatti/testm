package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.dao.impl.refbook.RefBookUtils;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookOktmoDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.utils.SimpleDateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Провайдер для больших справочников, которые хранятся в отдельных таблицах
 * Для окато
 *
 * @author dloshkarev
 */
@Service("RefBookOktmoProvider")
@Transactional
public class RefBookOktmoProvider implements RefBookDataProvider {

	// Справочник "ОКТМО"
	public static final Long OKTMO_REF_BOOK_ID = 96L;
	public static final String OKTMO_TABLE_NAME = "REF_BOOK_OKTMO";

    /** Код справочника */
    private Long refBookId;

    /** Название таблицы для запроса данных */
    private String tableName;

    @Autowired
    RefBookOktmoDao dao;

    @Autowired
    RefBookDao refBookDao;

    @Autowired
    RefBookUtils refBookUtils;

    @Autowired
    LogEntryService logEntryService;

    private static final ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        return dao.getRecords(getTableName(), refBookId, version, pagingParams, filter, sortAttribute, isSortAscending);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        return getRecords(version, pagingParams, filter, sortAttribute, true);
    }

    @Override
    public List<Pair<Long, Long>> getRecordIdPairs(Long refBookId, Date version, Boolean needAccurateVersion, String filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date getNextVersion(Date version, String filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Long> getUniqueRecordIds(Date version, String filter) {
        return dao.getUniqueRecordIds(OKTMO_REF_BOOK_ID, getTableName(), version, filter);
    }

    @Override
    public int getRecordsCount(Date version, String filter) {
        return dao.getRecordsCount(OKTMO_REF_BOOK_ID, getTableName(), version, filter);
    }

    @Override
    public List<Pair<Long, Long>> checkRecordExistence(Date version, String filter) {
        return dao.getRecordIdPairs(getTableName(), OKTMO_REF_BOOK_ID, version, null, filter);
    }

    @Override
    public List<Long> isRecordsExist(List<Long> uniqueRecordIds) {
        return refBookDao.isRecordsExist(getTableName(), new HashSet<Long>(uniqueRecordIds));
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getChildrenRecords(Long parentRecordId, Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute){
        return dao.getChildrenRecords(getTableName(), refBookId, version, parentRecordId, pagingParams, filter, sortAttribute);
    }

    @Override
    public Long getRowNum(Date version, Long recordId,
                          String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        return dao.getRowNum(getTableName(), refBookId, version, recordId, filter, sortAttribute, isSortAscending);
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
        RefBook refBook = refBookDao.get(refBookId);
        RefBookAttribute attribute = refBook.getAttribute(attributeId);
        Map<String, RefBookValue> value = dao.getRecordData(getTableName(), refBookId, recordId);
        return value != null ? value.get(attribute.getAlias()) : null;
    }

    @Override
    public List<Date> getVersions(Date startDate, Date endDate) {
        return dao.getVersions(getTableName(), startDate, endDate);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecordVersionsById(Long uniqueRecordId, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        return dao.getRecordVersions(getTableName(), refBookId, uniqueRecordId, pagingParams, filter, sortAttribute, true);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecordVersionsByRecordId(Long recordId, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        return dao.getRecordVersionsByRecordId(getTableName(), refBookId, recordId, pagingParams, filter, sortAttribute);
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
    }

    @Override
    public List<Long> createRecordVersionWithoutLock(Logger logger, Date versionFrom, Date versionTo, List<RefBookRecord> records) {
        throw new UnsupportedOperationException();
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
                        logger.info("Установлена дата окончания актуальности версии "+sdf.get().format(SimpleDateUtils.addDayToDate(versionFrom, -1))+" для предыдущей версии");
                    }
                }
            }
            if (result.getResult() == CrossResult.NEED_CHANGE) {
                refBookDao.updateVersionRelevancePeriod(getTableName(), result.getRecordId(), SimpleDateUtils.addDayToDate(versionTo, 1));
                updateResults(results, result);
            }
            if (result.getResult() == CrossResult.NEED_DELETE) {
                refBookDao.deleteRecordVersions(getTableName(), Arrays.asList(result.getRecordId()));
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
    public Map<Integer, List<Pair<RefBookAttribute, RefBookValue>>> getUniqueAttributeValues(Long uniqueRecordId) {
        Map<Integer, List<Pair<RefBookAttribute, RefBookValue>>> groups = new HashMap<Integer, List<Pair<RefBookAttribute, RefBookValue>>>();

        List<RefBookAttribute> attributes = refBookDao.getAttributes(refBookId);

        for (RefBookAttribute attribute : attributes) {
            if (attribute.getUnique() != 0) {

                List<Pair<RefBookAttribute, RefBookValue>> values = null;
                if (groups.get(attribute.getUnique()) != null) {
                    values = groups.get(attribute.getUnique());
                } else {
                    values = new ArrayList<Pair<RefBookAttribute, RefBookValue>>();
                }

                values.add(new Pair<RefBookAttribute, RefBookValue>(attribute, getValue(uniqueRecordId, attribute.getId())));
                groups.put(attribute.getUnique(), values);
            }
        }

        return groups;
    }

    @Override
    public void updateRecordVersion(Logger logger, Long uniqueRecordId, Date versionFrom, Date versionTo, Map<String, RefBookValue> values) {
        //TODO dloshkarev: надо все перепроверять. Т.к пока эти справочники read only, то эти методы не нужны
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRecordVersionWithoutLock(Logger logger, Long uniqueRecordId, Date versionFrom, Date versionTo, Map<String, RefBookValue> records) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRecordsVersionEnd(Logger logger, Date versionEnd, List<Long> uniqueRecordIds) {
        for (Long uniqueRecordId : uniqueRecordIds) {
            List<Long> relatedVersions = dao.getRelatedVersions(getTableName(), uniqueRecordIds);
            if (!relatedVersions.isEmpty() && relatedVersions.size() > 1) {
                refBookDao.deleteRecordVersions(getTableName(), relatedVersions);
            }
            Long recordId = dao.getRecordId(getTableName(), uniqueRecordId);
            crossVersionsProcessing(dao.checkCrossVersions(getTableName(), recordId, versionEnd, null, null),
                    versionEnd, null, logger);
            dao.createFakeRecordVersion(getTableName(), recordId, SimpleDateUtils.addDayToDate(versionEnd, 1));
        }
    }

    @Override
    public void updateRecordsVersionEndWithoutLock(Logger logger, Date versionEnd, List<Long> uniqueRecordIds) {
        updateRecordsVersionEnd(logger, versionEnd, uniqueRecordIds);
    }

    @Override
    public void deleteAllRecords(Logger logger, List<Long> uniqueRecordIds) {
        //TODO реализовать когда нужен будет импорт
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAllRecordsWithoutLock(Logger logger, List<Long> uniqueRecordIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteRecordVersions(Logger logger, List<Long> uniqueRecordIds, boolean force) {
        deleteRecordVersions(logger, uniqueRecordIds);
    }

    @Override
    public void deleteRecordVersions(Logger logger, List<Long> uniqueRecordIds) {
        //TODO dloshkarev: надо все перепроверять. Т.к пока эти справочники read only, то эти методы не нужны
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteRecordVersionsWithoutLock(Logger logger, List<Long> uniqueRecordIds) {
        throw new UnsupportedOperationException();
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
    public Map<RefBookAttributePair, String> getAttributesValues(List<RefBookAttributePair> attributePairs) {
        return dao.getAttributesValues(attributePairs);
    }

    @Override
    public List<ReferenceCheckResult> getInactiveRecordsInPeriod(@NotNull List<Long> recordIds, @NotNull Date periodFrom, Date periodTo) {
        return refBookDao.getInactiveRecordsInPeriod(getTableName(), recordIds, periodFrom, periodTo, false);
    }

    @Override
    public void insertRecords(TAUserInfo taUserInfo, Date version, List<Map<String, RefBookValue>> records) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void insertRecordsWithoutLock(TAUserInfo taUserInfo, Date version, List<Map<String, RefBookValue>> records) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRecords(TAUserInfo taUserInfo, Date version, List<Map<String, RefBookValue>> records) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRecordsWithoutLock(TAUserInfo taUserInfo, Date version, List<Map<String, RefBookValue>> records) {
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

	@Override
	public Map<Long, RefBookValue> dereferenceValues(Long attributeId, Collection<Long> recordIds) {
		return refBookDao.dereferenceValues(tableName, attributeId, recordIds);
	}

    @Override
    public List<String> getMatchedRecords(List<RefBookAttribute> attributes, List<Map<String, RefBookValue>> records, Integer accountPeriodId) {
        throw new UnsupportedOperationException();
    }
}
