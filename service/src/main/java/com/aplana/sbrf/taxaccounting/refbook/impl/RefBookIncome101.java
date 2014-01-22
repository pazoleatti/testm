package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookIncome101Dao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecordVersion;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Провайдер для справочника "Оборотная ведомость (Форма 0409101-СБ)"
 * Таблица INCOME_101
 * User: ekuvshinov
 */
@Service("refBookIncome101")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class RefBookIncome101 implements RefBookDataProvider {

    public static final Long REF_BOOK_ID = RefBookIncome101Dao.REF_BOOK_ID;

    @Autowired
    RefBookDao refBookDao;

    @Autowired
    private RefBookIncome101Dao refBookIncome101Dao;

    @Autowired
    private TaxPeriodDao taxPeriodDao;

    @Autowired
    private ReportPeriodDao reportPeriodDao;

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute, boolean isSortAscending) {
        return refBookIncome101Dao.getRecords(getReportPeriod(version).getId(), pagingParams, filter, sortAttribute, isSortAscending);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        return getRecords(version, pagingParams, filter, sortAttribute, true);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getChildrenRecords(Long parentRecordId, Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, RefBookValue> getRecordData(Long recordId) {
        return refBookIncome101Dao.getRecordData(recordId);
    }

    @Override
    public List<Date> getVersions(Date startDate, Date endDate) {
        List<Date> result = new ArrayList<Date>();
        List<ReportPeriod> reportPeriods = refBookIncome101Dao.gerReportPeriods();
        Calendar cal = new GregorianCalendar();
        for (ReportPeriod reportPeriod: reportPeriods) {
            TaxPeriod taxPeriod = reportPeriod.getTaxPeriod();
            cal.setTime(taxPeriod.getStartDate());
            cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + reportPeriod.getMonths());
            if (startDate.after(cal.getTime()) && endDate.before(cal.getTime()) && !result.contains(cal.getTime())) {
                result.add(cal.getTime());
            }
        }
        return result;
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecordVersions(Long recordId, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void insertRecords(Date version, List<Map<String, RefBookValue>> records) {
		throw new UnsupportedOperationException();
    }

    @Override
    public void updateRecords(Date version, List<Map<String, RefBookValue>> records) {
        refBookIncome101Dao.updateRecords(records);
    }

    @Override
    public void deleteRecords(Date version, List<Long> recordIds) {
		throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAllRecords(Date version) {
		throw new UnsupportedOperationException();
    }

    @Override
    public RefBookValue getValue(Long recordId, Long attributeId) {
        RefBook refBook = refBookDao.get(REF_BOOK_ID);
        RefBookAttribute attribute = refBook.getAttribute(attributeId);
        return refBookIncome101Dao.getRecordData(recordId).get(attribute.getAlias());
    }

    @Override
    public RefBookRecordVersion getRecordVersionInfo(Long uniqueRecordId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getRecordVersionsCount(Long refBookRecordId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void createRecordVersion(Logger logger, Long recordId, Date versionFrom, Date versionTo, List<Map<String, RefBookValue>> records) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Pair<RefBookAttribute, RefBookValue>> getUniqueAttributeValues(Long recordId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateRecordVersion(Logger logger, Long uniqueRecordId, Date versionFrom, Date versionTo, List<Map<String, RefBookValue>> records) {
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

    private ReportPeriod getReportPeriod(Date version) {
        List<TaxPeriod> taxPeriods = taxPeriodDao.listByTaxTypeAndDate(TaxType.INCOME, version, version);    // Данный справочник будет применятся в налоге на прибыль (Ф. Марат)
        if (taxPeriods.size() != 1) {
            throw new IllegalArgumentException("Invalid version for refbook");
        }
        TaxPeriod taxPeriod = taxPeriods.get(0);
        List<ReportPeriod> reportPeriods = reportPeriodDao.listByTaxPeriod(taxPeriod.getId());
        Calendar startCal = new GregorianCalendar();
        Long time = null;
        ReportPeriod reportPeriodResult = null;
        Long resultTime;
        for (ReportPeriod reportPeriod : reportPeriods) {
            startCal.setTime(taxPeriod.getStartDate());
            startCal.set(Calendar.MONTH, startCal.get(Calendar.MONTH) + reportPeriod.getMonths());
            resultTime = startCal.getTime().getTime() - version.getTime();
            if (resultTime >= 0 && ((reportPeriodResult == null) || (time > resultTime))) {
                time = resultTime;
                reportPeriodResult = reportPeriod;
            }
        }
        if (reportPeriodResult == null) {
            throw new IllegalArgumentException("Invalid version repord period not found");
        }
        return reportPeriodResult;
    }
}
