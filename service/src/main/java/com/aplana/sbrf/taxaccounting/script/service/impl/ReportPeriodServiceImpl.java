package com.aplana.sbrf.taxaccounting.script.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.ReportPeriodType;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.script.service.ReportPeriodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.GregorianCalendar;


@Service("reportPeriodService")
@Transactional(readOnly = true)
public class ReportPeriodServiceImpl extends AbstractDao implements ReportPeriodService {

    @Autowired
    private ReportPeriodDao reportPeriodDao;

    @Autowired(required = false)
    private com.aplana.sbrf.taxaccounting.service.PeriodService reportPeriodService;

    @Override
    public ReportPeriod get(int reportPeriodId) {
        return reportPeriodDao.fetchOne(reportPeriodId);
    }

    @Override
    public Calendar getStartDate(int reportPeriodId) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(reportPeriodService.fetchReportPeriod(reportPeriodId).getStartDate());
        return cal;
    }

    @Override
    public Calendar getCalendarStartDate(int reportPeriodId) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(reportPeriodService.fetchReportPeriod(reportPeriodId).getCalendarStartDate());
        return cal;
    }

    @Override
    public Calendar getEndDate(int reportPeriodId) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(reportPeriodService.fetchReportPeriod(reportPeriodId).getEndDate());
        return cal;
    }

    @Override
    public ReportPeriod getByTaxTypedCodeYear(TaxType taxType, String code, int year) {
        return reportPeriodService.getByDictCodeAndYear(code, year);
    }

    @Override
    public ReportPeriodType getReportPeriodTypeById(Long id) {
        return reportPeriodDao.getReportPeriodTypeById(id);
    }
}
