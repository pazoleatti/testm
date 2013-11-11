package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.impl.refbook.RefBookIncome102DaoImpl;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookIncome102Dao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Провайдер для справочника "Отчет о прибылях и убытках (Форма 0409102-СБ)"
 * Таблица INCOME_102
 * @author Dmitriy Levykin
 */
@Service("refBookIncome102")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class RefBookIncome102 implements RefBookDataProvider {

    public static final Long REF_BOOK_ID = RefBookIncome102Dao.REF_BOOK_ID;

	@Autowired
    RefBookDao rbDao;

    @Autowired
    private RefBookIncome102Dao refBookIncome102Dao;

    @Autowired
    private TaxPeriodDao taxPeriodDao;
    @Autowired
    private ReportPeriodDao reportPeriodDao;

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        return refBookIncome102Dao.getRecords(getReportPeriod(version).getId(), pagingParams, filter, sortAttribute);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getChildrenRecords(Long parentRecordId, Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
		throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, RefBookValue> getRecordData(Long recordId) {
        return refBookIncome102Dao.getRecordData(recordId);
    }

    @Override
    public List<Date> getVersions(Date startDate, Date endDate) {
        List<Date> result = new ArrayList<Date>();
        List<ReportPeriod> reportPeriods = refBookIncome102Dao.gerReportPeriods();
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
    public void insertRecords(Date version, List<Map<String, RefBookValue>> records) {
		throw new UnsupportedOperationException();
    }

    @Override
    public void updateRecords(Date version, List<Map<String, RefBookValue>> records) {
        refBookIncome102Dao.updateRecords(records);
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
        RefBook refBook = rbDao.get(REF_BOOK_ID);
        RefBookAttribute attribute = refBook.getAttribute(attributeId);
        return refBookIncome102Dao.getRecordData(recordId).get(attribute.getAlias());
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
            startCal.clear();
            startCal.set(Calendar.YEAR, reportPeriod.getYear());
            startCal.set(Calendar.MONTH, startCal.get(Calendar.MONTH) + reportPeriod.getMonths());
            resultTime = startCal.getTime().getTime() - version.getTime();
            if (resultTime >= 0 && ((reportPeriodResult == null) || (time > resultTime))) {
                time = resultTime;
                reportPeriodResult = reportPeriod;
            }
        }
        if (reportPeriodResult == null) {
            throw new IllegalArgumentException("Invalid version report period not found");
        }
        return reportPeriodResult;
    }
}
