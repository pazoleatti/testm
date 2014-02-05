package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookIncome102Dao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookOktmoDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Провайдер для справочника ОКТМО
 *
 * @author auldanov
 */
@Service("RefBookOktmo")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class RefBookOktmo implements RefBookDataProvider {

    public static final Long REF_BOOK_ID = RefBookIncome102Dao.REF_BOOK_ID;

    @Autowired
    RefBookOktmoDao dao;

    @Autowired
    RefBookDao rbDao;

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        return dao.getRecords(version, pagingParams, filter, sortAttribute, isSortAscending);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        return getRecords(version, pagingParams, filter, sortAttribute, true);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getChildrenRecords(Long parentRecordId, Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute){
        return dao.getChildrenRecords(parentRecordId, version, pagingParams, filter, sortAttribute);
    }

    @Override
    public Map<String, RefBookValue> getRecordData(Long recordId) {
        return dao.getRecordData(recordId);
    }

    @Override
    public RefBookValue getValue(Long recordId, Long attributeId) {
        RefBook refBook = rbDao.get(REF_BOOK_ID);
        RefBookAttribute attribute = refBook.getAttribute(attributeId);
        return dao.getRecordData(recordId).get(attribute.getAlias());
    }

    @Override
    public List<Date> getVersions(Date startDate, Date endDate) {
        return dao.getVersions(startDate, endDate);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecordVersions(Long uniqueRecordId, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        return dao.getRecordVersions(uniqueRecordId, pagingParams, filter, sortAttribute);
    }

    @Override
    public RefBookRecordVersion getRecordVersionInfo(Long uniqueRecordId) {
        return dao.getRecordVersionInfo(uniqueRecordId);
    }

    @Override
    public Map<Long, Date> getRecordsVersionStart(List<Long> uniqueRecordIds) {
        // возможно стоит оптимизировать и вынести в дао
        final Map<Long, Date> result = new HashMap<Long, Date>();
        for (Long id: uniqueRecordIds){
            result.put(id, getRecordVersionInfo(id).getVersionStart());
        }

        return result;
    }

    @Override
    public int getRecordVersionsCount(Long uniqueRecordId) {
        return dao.getRecordVersionsCount(uniqueRecordId);
    }

    @Override
    public void createRecordVersion(Logger logger, Date versionFrom, Date versionTo, List<RefBookRecord> records) {
        // TODO реализовать
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Pair<RefBookAttribute, RefBookValue>> getUniqueAttributeValues(Long uniqueRecordId) {
        List<Pair<RefBookAttribute, RefBookValue>> values = new ArrayList<Pair<RefBookAttribute, RefBookValue>>();
        List<RefBookAttribute> attributes =  rbDao.getAttributes(REF_BOOK_ID);
        for (RefBookAttribute attribute : attributes) {
            if (attribute.isUnique()) {
                values.add(new Pair<RefBookAttribute, RefBookValue>(attribute, getValue(uniqueRecordId, attribute.getId())));
            }
        }
        return values;
    }

    @Override
    public void updateRecordVersion(Logger logger, Long uniqueRecordId, Date versionFrom, Date versionTo, Map<String, RefBookValue> records) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRecordsVersionEnd(Logger logger, Date versionEnd, List<Long> uniqueRecordIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAllRecords(Logger logger, List<Long> uniqueRecordIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteRecordVersions(Logger logger, List<Long> uniqueRecordIds) {
        throw new UnsupportedOperationException();
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
    public void insertRecords(Date version, List<Map<String, RefBookValue>> records) {
        dao.insertRecords(version, records);
    }

    @Override
    public void updateRecords(Date version, List<Map<String, RefBookValue>> records) {

    }

    @Override
    public void deleteRecords(Date version, List<Long> recordIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAllRecords(Date version) {
        dao.deleteAllRecords(version);
    }
}
