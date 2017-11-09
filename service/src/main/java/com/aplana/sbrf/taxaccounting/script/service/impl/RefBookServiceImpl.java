package com.aplana.sbrf.taxaccounting.script.service.impl;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDepartmentDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.CheckCrossVersionsResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecord;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecordVersion;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.refbook.RefBookHelper;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.script.service.RefBookService;
import com.aplana.sbrf.taxaccounting.script.service.util.ScriptUtils;
import com.aplana.sbrf.taxaccounting.service.TransactionHelper;
import com.aplana.sbrf.taxaccounting.service.TransactionLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service("refBookService")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RefBookServiceImpl implements RefBookService {

    @Autowired
    private RefBookFactory factory;

    @Autowired
    private RefBookDao refBookDao;

    @Autowired
    private RefBookHelper refBookHelper;

    @Autowired
    private TransactionHelper transactionHelper;

    @Autowired
    private RefBookDepartmentDao refBookDepartmentDao;

    @Autowired
    private TAUserService taUserService;

    @Override
    public Map<String, RefBookValue> getRecordData(Long refBookId, Long recordId) {
        try{
            return factory.getDataProvider(refBookId).getRecordData(recordId);
        } catch (DaoException e){
            return null;
        }
    }

    @Override
    public String getStringValue(Long refBookId, Long recordId, String alias) {
        RefBookValue refBookValue = getValue(refBookId, recordId, alias);
        return refBookValue != null ? refBookValue.getStringValue() : null;
    }

    @Override
    public Number getNumberValue(Long refBookId, Long recordId, String alias) {
        RefBookValue refBookValue = getValue(refBookId, recordId, alias);
        return refBookValue != null ? refBookValue.getNumberValue() : null;
    }

    @Override
    public Date getDateValue(Long refBookId, Long recordId, String alias) {
        RefBookValue refBookValue = getValue(refBookId, recordId, alias);
        return refBookValue != null ? refBookValue.getDateValue() : null;
    }

    @Override
    public void dataRowsDereference(Logger logger, Collection<DataRow<Cell>> dataRows, List<Column> columns) {
        refBookHelper.dataRowsDereference(logger, dataRows, columns);
    }

    @Override
    public void executeInNewTransaction(TransactionLogic logic) {
        transactionHelper.executeInNewTransaction(logic);
    }

    @Override
    public <T> T returnInNewTransaction(TransactionLogic<T> logic) {
        return transactionHelper.executeInNewTransaction(logic);
    }

    @Override
    public List<Pair<String, String>> getMatchedRecordsByUniqueAttributes(Long recordId, List<RefBookAttribute> attributes, List<RefBookRecord> records) {
        return refBookDepartmentDao.getMatchedRecordsByUniqueAttributes(recordId, attributes, records);
    }

    @Override
    public RefBookRecordVersion getNextVersion(Long refBookId, Long recordId, Date versionFrom) {
        return refBookDao.getNextVersion(refBookId, recordId, versionFrom);
    }

    @Override
    public List<CheckCrossVersionsResult> checkCrossVersions(Long refBookId, Long recordId, Date versionFrom, Date versionTo, Long excludedRecordId) {
        return refBookDao.checkCrossVersions(refBookId, recordId, versionFrom, versionTo, excludedRecordId);
    }

    @Override
    public void updateVersionRelevancePeriod(String tableName, Long uniqueRecordId, Date version) {
        refBookDao.updateVersionRelevancePeriod(tableName, uniqueRecordId, version);
    }

    @Override
    public void deleteRecordVersions(String tableName, List<Long> uniqueRecordIds) {
        refBookDao.deleteRecordVersions(tableName, uniqueRecordIds, false);
    }

    @Override
    public List<String> isVersionUsedInDepartmentConfigs(Long refBookId, List<Long> uniqueRecordIds, Date versionFrom, Date versionTo, Boolean restrictPeriod, List<Long> excludeUseCheck) {
        return refBookDao.isVersionUsedInDepartmentConfigs(refBookId, uniqueRecordIds, versionFrom, versionTo, restrictPeriod, excludeUseCheck);
    }

    @Override
    public RefBookRecordVersion getPreviousVersion(Long refBookId, Long recordId, Date versionFrom) {
        return refBookDao.getPreviousVersion(refBookId, recordId, versionFrom);
    }

    @Override
    public Long findRecord(Long refBookId, Long recordId, Date version) {
        return refBookDao.findRecord(refBookId, recordId, version);
    }

    @Override
    public List<Long> getRelatedVersions(List<Long> uniqueRecordIds) {
        return refBookDao.getRelatedVersions(uniqueRecordIds);
    }

    @Override
    public boolean isVersionsExist(Long refBookId, List<Long> recordIds, Date version) {
        return refBookDao.isVersionsExist(refBookId, recordIds, version);
    }

    @Override
    public void createFakeRecordVersion(Long refBookId, Long recordId, Date version) {
        refBookDao.createFakeRecordVersion(refBookId, recordId, version);
    }

    @Override
    public void updateRecordVersion(Long refBookId, Long uniqueRecordId, Map<String, RefBookValue> records) {
        refBookDao.updateRecordVersion(refBookId, uniqueRecordId, records);
    }

    private RefBookValue getValue(Long refBookId, Long recordId, String alias) {
        if (refBookId == null || recordId == null || alias == null || alias.isEmpty())
            return null;

        Map<String, RefBookValue> map = getRecordData(refBookId, recordId);

        if (map == null || map.isEmpty() || !map.containsKey(alias))
            return null;

        return map.get(alias);
    }

    @Override
    public Map<String, RefBookValue> getRefBookValue(long refBookId, Long recordId,
                                                     Map<String, Map<String, RefBookValue>> refBookCache) {
        if (recordId == null) {
            return null;
        }
        String key = ScriptUtils.getRefBookCacheKey(refBookId, recordId);
        if (!refBookCache.containsKey(key)) {
            refBookCache.put(key, getRecordData(refBookId, recordId));
        }
        return refBookCache.get(key);
    }

    @Override
    public PagingResult<TAUserView> getUsersByFilter(MembersFilterData filter) {
        return taUserService.getUsersByFilter(filter);
    }
}
