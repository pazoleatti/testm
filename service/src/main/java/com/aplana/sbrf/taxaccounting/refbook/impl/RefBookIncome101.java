package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookIncome101Dao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * Провайдер для справочника "Оборотная ведомость (Форма 0409101-СБ)"
 * Таблица INCOME_101
 * User: ekuvshinov
 */
@Service("refBookIncome101")
@Transactional
public class RefBookIncome101 implements RefBookDataProvider {

    public static final Long REF_BOOK_ID = RefBookIncome101Dao.REF_BOOK_ID;
	public static final String TABLE_NAME = RefBookIncome101Dao.TABLE_NAME;

	@Autowired
	private RefBookDao refBookDao;

    @Autowired
    private RefBookIncome101Dao dao;

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        return dao.getRecords(pagingParams, filter, sortAttribute, isSortAscending);
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
         return dao.getUniqueRecordIds(filter);
    }

    @Override
    public int getRecordsCount(Date version, String filter) {
            return dao.getRecordsCount(filter);
    }

    @Override
    public List<Pair<Long, Long>> checkRecordExistence(Date version, String filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Long> isRecordsExist(List<Long> uniqueRecordIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getChildrenRecords(Long parentRecordId, Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long getRowNum(Date version, Long recordId,
                          String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Long> getParentsHierarchy(Long uniqueRecordId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, RefBookValue> getRecordData(Long recordId) {
        return dao.getRecordData(recordId);
    }

    @Override
    public Map<Long, Map<String, RefBookValue>> getRecordData(List<Long> recordIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Date> getVersions(Date startDate, Date endDate) {
        return dao.getVersions(startDate, endDate);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecordVersionsById(Long recordId, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecordVersionsByRecordId(Long recordId, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        throw new UnsupportedOperationException();
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
        dao.updateRecords(records);
    }

    @Override
    public void updateRecordsWithoutLock(TAUserInfo taUserInfo, Date version, List<Map<String, RefBookValue>> records) {
        updateRecords(taUserInfo, version, records);
    }

    @Override
    public RefBookValue getValue(Long recordId, Long attributeId) {
        RefBook refBook = refBookDao.get(REF_BOOK_ID);
        RefBookAttribute attribute = refBook.getAttribute(attributeId);
        return dao.getRecordData(recordId).get(attribute.getAlias());
    }

    @Override
    public RefBookRecordVersion getRecordVersionInfo(Long uniqueRecordId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<Long, Date> getRecordsVersionStart(List<Long> uniqueRecordIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getRecordVersionsCount(Long refBookRecordId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Long> createRecordVersion(Logger logger, Date versionFrom, Date versionTo, List<RefBookRecord> records) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Long> createRecordVersionWithoutLock(Logger logger, Date versionFrom, Date versionTo, List<RefBookRecord> records) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<Integer, List<Pair<RefBookAttribute, RefBookValue>>> getUniqueAttributeValues(Long recordId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRecordVersion(Logger logger, Long uniqueRecordId, Date versionFrom, Date versionTo, Map<String, RefBookValue> records) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRecordVersionWithoutLock(Logger logger, Long uniqueRecordId, Date versionFrom, Date versionTo, Map<String, RefBookValue> records) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRecordsVersionEnd(Logger logger, Date versionEnd, List<Long> uniqueRecordIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRecordsVersionEndWithoutLock(Logger logger, Date versionEnd, List<Long> uniqueRecordIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAllRecords(Logger logger, List<Long> uniqueRecordIds) {
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
        dao.deleteRecords(uniqueRecordIds);
    }

    @Override
    public void deleteRecordVersionsWithoutLock(Logger logger, List<Long> uniqueRecordIds) {
        deleteRecordVersions(logger, uniqueRecordIds);
    }

    @Override
    public Long getFirstRecordId(Long uniqueRecordId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long getRecordId(Long uniqueRecordId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<RefBookAttributePair, String> getAttributesValues(List<RefBookAttributePair> attributePairs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<Long, CheckResult>  getInactiveRecordsInPeriod(@NotNull List<Long> recordIds, @NotNull Date periodFrom, Date periodTo) {
        throw new UnsupportedOperationException();
    }

	@Override
	public Map<Long, RefBookValue> dereferenceValues(Long attributeId, Collection<Long> recordIds) {
		return refBookDao.dereferenceValues(TABLE_NAME, attributeId, recordIds);
	}

    @Override
    public List<String> getMatchedRecords(List<RefBookAttribute> attributes, List<Map<String, RefBookValue>> records, Integer accountPeriodId) {
        throw new UnsupportedOperationException();
    }
}
