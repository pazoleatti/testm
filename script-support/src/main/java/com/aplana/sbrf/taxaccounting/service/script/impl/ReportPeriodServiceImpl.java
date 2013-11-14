package com.aplana.sbrf.taxaccounting.service.script.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.service.script.ReportPeriodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.List;


/*
 * Реализация ReportPeriodService
 * @author auldanov
 */
@Service("reportPeriodService")
@Transactional(readOnly = true)
public class ReportPeriodServiceImpl extends AbstractDao implements ReportPeriodService {

	@Autowired
	ReportPeriodDao reportPeriodDao;

	@Autowired
	TaxPeriodDao taxPeriodDao;

    @Autowired(required = false)
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
		// текущий отчетный период
		ReportPeriod thisReportPeriod = reportPeriodDao.get(reportPeriodId);
		// текущий налоговый период
		TaxPeriod thisTaxPeriod = thisReportPeriod.getTaxPeriod();
		// список отчетных периодов в текущем налоговом периоде
		List<ReportPeriod> reportPeriodlist = reportPeriodDao.listByTaxPeriod(thisReportPeriod.getTaxPeriod().getId());

		/**
		 *  если это первый отчетный период в данном налоговом периоде
		 *  то возвращать последний отчетный период с предыдущего налогово периода
		 */
		if (reportPeriodlist.size() > 0 && reportPeriodlist.get(0).getId() == reportPeriodId){
			List<TaxPeriod> taxPeriodlist = taxPeriodDao.listByTaxType(thisTaxPeriod.getTaxType());
			for (int i = 0; i < taxPeriodlist.size(); i++){
				if (taxPeriodlist.get(i).getId().equals(thisTaxPeriod.getId())){
                    if (i == 0) {
                        return null;
                    }
					// получим список отчетных периодов для данного налогового периода
					reportPeriodlist = reportPeriodDao.listByTaxPeriod(taxPeriodlist.get(i - 1).getId());
					// вернем последний отчетный период
					return reportPeriodlist.size() > 0 ? reportPeriodlist.get(reportPeriodlist.size() - 1) : null;
				}
			}
		} else {
            // не первый отчетный период в данном налоговом
            for (int i = 0; i < reportPeriodlist.size(); i++){
                if (reportPeriodlist.get(i).getId().equals(reportPeriodId)) {
                    return reportPeriodlist.get(i - 1);
                }
            }
        }
		return null;
	}

    /**
     * Возвращает дату начала отчетного периода
     * Дата высчитывается прибавлением смещения в месяцах к дате налогового периода
     * Смещение в месяцах вычисляется путем суммирования длительности предыдущих
     * отчетных периодов в данном налоговом периоде.
     *
     * Для отчетных периодов относящихся к налоговому периоду с типом "налог на прибыль"
     * смещение считается по другому алгоритму.
     *
     * <p>Информация о периодах в конфлюенсе
     * <a href="http://conf.aplana.com/pages/viewpage.action?pageId=9600466">Как считать отчетные периоды для разных налогов</a><p/>
     *
     * @param reportPeriodId
     * @return
     */
    @Override
    public Calendar getStartDate(int reportPeriodId){
        return reportPeriodService.getStartDate(reportPeriodId);
    }

    /**
     * Возвращает дату конца отчетного периода
     * <p>Информация о периодах в конфлюенсе
     * <a href="http://conf.aplana.com/pages/viewpage.action?pageId=9600466">Как считать отчетные периоды для разных налогов</a><p/>
     *
     * @param reportPeriodId
     * @return
     */
    @Override
    public Calendar getEndDate(int reportPeriodId){
       return reportPeriodService.getEndDate(reportPeriodId);
    }

    @Override
    public Calendar getReportDate(int reportPeriodId) {
       return reportPeriodService.getReportDate(reportPeriodId);
    }

    @Override
    public boolean isActivePeriod(int reportPeriodId, long departmentId) {
        return reportPeriodService.isActivePeriod(reportPeriodId, departmentId);  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isBalancePeriod(int reportPeriodId, long departmentId) {
        return reportPeriodService.isBalancePeriod(reportPeriodId, departmentId);
    }

    @Override
    public Calendar getMonthStartDate(FormData formData) {
        return reportPeriodService.getMonthStartDate(formData);
    }

    @Override
    public Calendar getMonthEndDate(FormData formData) {
        return reportPeriodService.getMonthEndDate(formData);
    }

    @Override
    public Calendar getMonthReportDate(FormData formData) {
        return reportPeriodService.getMonthReportDate(formData);
    }
}
