package com.aplana.sbrf.taxaccounting.service.script.impl;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDepartmentDao;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormLink;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.refbook.RefBookHelper;
import com.aplana.sbrf.taxaccounting.service.script.RefBookService;
import com.aplana.sbrf.taxaccounting.util.TransactionHelper;
import com.aplana.sbrf.taxaccounting.util.TransactionLogic;
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
    public List<FormLink> isVersionUsedInForms(Long refBookId, List<Long> uniqueRecordIds, Date versionFrom, Date versionTo, Boolean restrictPeriod) {
        return refBookDao.isVersionUsedInForms(refBookId, uniqueRecordIds, versionFrom, versionTo, restrictPeriod);
    }

    @Override
    public List<String> isVersionUsedInRefBooks(Long refBookId, List<Long> uniqueRecordIds, Date versionFrom, Date versionTo, Boolean restrictPeriod, List<Long> excludeUseCheck) {
        return refBookDao.isVersionUsedInRefBooks(refBookId, uniqueRecordIds, versionFrom, versionTo, restrictPeriod, excludeUseCheck);
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
}
