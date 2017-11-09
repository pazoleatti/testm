package com.aplana.sbrf.taxaccounting.script.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.script.service.ReportPeriodService;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/*
 * Реализация ReportPeriodService
 * @author auldanov
 */
@Service("reportPeriodService")
@Transactional(readOnly = true)
public class ReportPeriodServiceImpl extends AbstractDao implements ReportPeriodService {

	@Autowired
    private
    ReportPeriodDao reportPeriodDao;

	@Autowired
	TaxPeriodDao taxPeriodDao;

    @Autowired
    private
    DepartmentReportPeriodDao departmentReportPeriodDao;

    @Autowired(required = false)
    private
    com.aplana.sbrf.taxaccounting.service.PeriodService reportPeriodService;
	
	@Override
	public ReportPeriod get(int reportPeriodId) {
		return reportPeriodDao.get(reportPeriodId);
	}

	@Override
	public List<ReportPeriod> listByTaxPeriod(int taxPeriodId) {
		return reportPeriodDao.listByTaxPeriod(taxPeriodId);
	}

	@Override
	public ReportPeriod getPrevReportPeriod(int reportPeriodId) {
        return reportPeriodService.getPrevReportPeriod(reportPeriodId);
	}

    /**
     * Возвращает дату начала отчетного периода
     *
     * <p>Информация о периодах в конфлюенсе
     * <a href="http://conf.aplana.com/pages/viewpage.action?pageId=9600466">Как считать отчетные периоды для разных налогов</a><p/>
     *
     * @param reportPeriodId
     * @return
     */
    @Override
    public Calendar getStartDate(int reportPeriodId){
		Calendar cal = new GregorianCalendar();
		cal.setTime(reportPeriodService.getReportPeriod(reportPeriodId).getStartDate().toDate());
		return cal;
    }

    /**
     * Возвращает календарную дату начала отчетного периода. Для налога по прибыли.
     * @param reportPeriodId
     */
    @Override
    public Calendar getCalendarStartDate(int reportPeriodId){
		Calendar cal = new GregorianCalendar();
		cal.setTime(reportPeriodService.getReportPeriod(reportPeriodId).getCalendarStartDate().toDate());
		return cal;
    }

    /**
     * Возвращает дату конца отчетного периода
     * <p>Информация о периодах в конфлюенсе
     * <a href="http://conf.aplana.com/pages/viewpage.action?pageId=9600466">Как считать отчетные периоды для разных налогов</a><p/>
     * @param reportPeriodId
     */
    @Override
    public Calendar getEndDate(int reportPeriodId){
		Calendar cal = new GregorianCalendar();
		cal.setTime(reportPeriodService.getReportPeriod(reportPeriodId).getEndDate().toDate());
		return cal;
    }

    @Override
    public Calendar getReportDate(int reportPeriodId) {
       return reportPeriodService.getReportDate(reportPeriodId);
    }

    @Override
    public Calendar getMonthStartDate(int reportPeriodId, int periodOrder) {
        return reportPeriodService.getMonthStartDate(reportPeriodId, periodOrder);
    }

    @Override
    public Calendar getMonthEndDate(int reportPeriodId, int periodOrder) {
        return reportPeriodService.getMonthEndDate(reportPeriodId, periodOrder);
    }

    @Override
    public Calendar getMonthReportDate(int reportPeriodId, int periodOrder) {
        return reportPeriodService.getMonthReportDate(reportPeriodId, periodOrder);
    }

    @Override
    public List<ReportPeriod> getReportPeriodsByDate(TaxType taxType, LocalDateTime startDate, LocalDateTime endDate) {
        return reportPeriodDao.getReportPeriodsByDate(taxType, startDate, endDate);
    }

    @Override
    public Integer getCorrectionNumber(int departmentReportPeriodId) {
        return departmentReportPeriodDao.getCorrectionNumber(departmentReportPeriodId);
    }

    @Override
    public ReportPeriod getByTaxTypedCodeYear(TaxType taxType, String code, int year) {
        return reportPeriodService.getByTaxTypedCodeYear(taxType, code, year);
    }
}
