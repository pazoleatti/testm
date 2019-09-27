package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.ReportPeriodType;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.service.ReportPeriodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.GregorianCalendar;

@Service
@Transactional(readOnly = true)
public class ReportPeriodServiceImpl implements ReportPeriodService {

    @Autowired
    private ReportPeriodDao reportPeriodDao;

    @Override
    public ReportPeriod fetchOrCreate(TaxPeriod taxPeriod, ReportPeriodType reportPeriodType, Integer formTypeId) {
        ReportPeriod reportPeriod = reportPeriodDao.fetchOneByTaxPeriodAndDict(taxPeriod.getId(), reportPeriodType.getId());
        if (reportPeriod == null) {
            reportPeriod = new ReportPeriod();
            reportPeriod.setName(reportPeriodType.getName());

            // Устанавливаем дату начала, окончания и календарную дату начала периода
            // в соответствии с типом отчетного периода из справочника
            GregorianCalendar gregorianCalendar = new GregorianCalendar();
            Calendar start = Calendar.getInstance();
            start.setTime(reportPeriodType.getStartDate());
            start.set(Calendar.YEAR, taxPeriod.getYear());

            Calendar end = Calendar.getInstance();
            end.setTime(reportPeriodType.getEndDate());
            end.set(Calendar.YEAR, taxPeriod.getYear());

            Calendar calendarDate = Calendar.getInstance();
            calendarDate.setTime(reportPeriodType.getCalendarStartDate());
            calendarDate.set(Calendar.YEAR, taxPeriod.getYear());

            if (gregorianCalendar.isLeapYear(taxPeriod.getYear())) {
                if (start.get(Calendar.MONTH) == Calendar.FEBRUARY && start.get(Calendar.DATE) == 28) {
                    start.set(Calendar.DATE, 29);
                }
                if (end.get(Calendar.MONTH) == Calendar.FEBRUARY && end.get(Calendar.DATE) == 28) {
                    end.set(Calendar.DATE, 29);
                }
                if (calendarDate.get(Calendar.MONTH) == Calendar.FEBRUARY && calendarDate.get(Calendar.DATE) == 28) {
                    calendarDate.set(Calendar.DATE, 29);
                }
            }
            reportPeriod.setStartDate(start.getTime());
            reportPeriod.setEndDate(end.getTime());
            reportPeriod.setCalendarStartDate(calendarDate.getTime());
            reportPeriod.setTaxPeriod(taxPeriod);
            reportPeriod.setDictTaxPeriodId(reportPeriodType.getId());
            reportPeriod.setReportPeriodTaxFormTypeId(formTypeId);
            reportPeriod = reportPeriodDao.fetchOne(reportPeriodDao.create(reportPeriod));
        }
        return reportPeriod;
    }
}
