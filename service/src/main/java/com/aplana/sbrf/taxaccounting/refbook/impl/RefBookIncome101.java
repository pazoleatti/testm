package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.dao.api.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookIncome101Dao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Провайдер для справочника "Оборотная ведомость (Форма 0409101-СБ)"
 * Таблица INCOME_101
 * User: ekuvshinov
 */
@Service("refBookIncome101")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RefBookIncome101 implements RefBookDataProvider {
    public final static long REF_BOOK_ID = 50L;

    @Autowired
    RefBookDao rbDao;

    @Autowired
    private RefBookIncome101Dao bookBookerStatemensDao;

    @Autowired
    private TaxPeriodDao taxPeriodDao;

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        return bookBookerStatemensDao.getRecords(pagingParams, filter, sortAttribute);
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getChildrenRecords(Long parentRecordId, Date version, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute) {
        return null;  // Этот метод нужен для иерархического справочника и тут он не нужен (Ф. Марат)
    }

    @Override
    public Map<String, RefBookValue> getRecordData(Long recordId) {
        return bookBookerStatemensDao.getRecordData(recordId);
    }

    @Override
    public List<Date> getVersions(Date startDate, Date endDate) {
        List<Date> result = new ArrayList<Date>();
        List<ReportPeriod> reportPeriods = bookBookerStatemensDao.gerReportPeriods();
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
        // Не требуется
    }

    @Override
    public void updateRecords(Date version, List<Map<String, RefBookValue>> records) {
        bookBookerStatemensDao.updateRecords(records);
    }

    @Override
    public void deleteRecords(Date version, List<Long> recordIds) {
        // Не требуется
    }

    @Override
    public void deleteAllRecords(Date version) {
        // Не требуется
    }

    @Override
    public RefBookValue getValue(Long recordId, Long attributeId) {
        RefBook refBook = rbDao.get(REF_BOOK_ID);
        RefBookAttribute attribute = refBook.getAttribute(attributeId);
        return bookBookerStatemensDao.getRecordData(recordId).get(attribute.getAlias());
    }
}
