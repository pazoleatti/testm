package com.aplana.sbrf.taxaccounting.service.script.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
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
    com.aplana.sbrf.taxaccounting.service.ReportPeriodService reportPeriodService;
	
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
		ReportPeriod thisReportPeriod= reportPeriodDao.get(reportPeriodId);
		// текущий налоговый период
		TaxPeriod thisTaxPeriod = taxPeriodDao.get(thisReportPeriod.getTaxPeriodId());
		// список отчетных периодов в текущем налоговом периоде
		List<ReportPeriod> reportPeriodlist = reportPeriodDao.listByTaxPeriod(thisReportPeriod.getTaxPeriodId());
		
		/**
		 *  если это первый отчетный период в данном налоговом периоде
		 *  то возвращать последний отчетный период с предыдущего налогово периода
		 */
		if (reportPeriodlist.size() > 0 && reportPeriodlist.get(0).getId() == reportPeriodId){
			List<TaxPeriod> taxPeriodlist = taxPeriodDao.listByTaxType(thisTaxPeriod.getTaxType());
			for (int i = 0; i < taxPeriodlist.size()-1; i++){
				if (taxPeriodlist.get(i).getId() == thisTaxPeriod.getId()){
					// получим список отчетных периодов для данного налогового периода
					reportPeriodlist = reportPeriodDao.listByTaxPeriod(taxPeriodlist.get(i+1).getId());
					// вернем последний отчетный период
					return reportPeriodlist.get(0);
				}
			}
		}
		
		// не первый отчетный период в данныом налоговом
		for (int i = 0; i < reportPeriodlist.size(); i++){
			if (reportPeriodlist.get(i).getId() == reportPeriodId && i!=0){
				return reportPeriodlist.get(i-1);
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
     * смещение считается по другому алгоритму
     * @param reportPeriodId
     * @return
     */
    public Calendar getStartDate(int reportPeriodId){
    	 ReportPeriod reportPeriod = reportPeriodDao.get(reportPeriodId);
         TaxPeriod taxPeriod = taxPeriodDao.get(reportPeriod.getTaxPeriodId());
         // календарь
         Calendar cal = Calendar.getInstance();
         cal.setTime(taxPeriod.getStartDate());

         // для налога на прибыль, периоды вложены в друг дгруга, и начало всегда совпадает
         if (taxPeriod.getTaxType() != TaxType.INCOME){
             // получим отчетные периоды для данного налогового периода
             List<ReportPeriod> reportPeriodList = reportPeriodDao.listByTaxPeriod(reportPeriod.getTaxPeriodId());
             // смещение относительно налогового периода
             int months = 0;
             for (ReportPeriod cReportPeriod: reportPeriodList){
                 // если достигли текущего то выходим из цикла
                 if (cReportPeriod.getId() == reportPeriod.getId()){
                     break;
                 }
                 // смещение в месяцах
                 months += cReportPeriod.getMonths();
             }
             cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + months);
         }

         return cal;
    }

    /**
     * Возвращает дату конца отчетного периода
     * Дата высчитывается прибавлением смещения в месяцах к дате налогового периода
     * Смещение в месяцах вычисляется путем суммирования длительности предыдущих
     * отчетных периодов в данном налоговом периоде.
     *
     * Для отчетных периодов относящихся к налоговому периоду с типом "налог на прибыль"
     * смещение считается по другому алгоритму
     *
     * @param reportPeriodId
     * @return
     */
    public Calendar getEndDate(int reportPeriodId){
        ReportPeriod reportPeriod = reportPeriodDao.get(reportPeriodId);
        TaxPeriod taxPeriod = taxPeriodDao.get(reportPeriod .getTaxPeriodId());
        // календарь
        Calendar cal = Calendar.getInstance();
        cal.setTime(taxPeriod.getStartDate());

        // для налога на прибыль, периоды вложены в друг дгруга, и начало всегда совпадает
        if (taxPeriod.getTaxType() == TaxType.INCOME){
            cal.set(Calendar.MONTH,  reportPeriod.getMonths());
        }
        else{
            // получим отчетные периоды для данного налогового периода
            List<ReportPeriod> reportPeriodList = reportPeriodDao.listByTaxPeriod(reportPeriod.getTaxPeriodId());
            // смещение относительно налогового периода
            int months = 0;
            for (ReportPeriod cReportPeriod: reportPeriodList){
                // если достигли текущего то выходим из цикла
                if (cReportPeriod.getId() == reportPeriod.getId()){
                    months += cReportPeriod.getMonths();
                    break;
                }
                // смещение в месяцах
                months += cReportPeriod.getMonths();
            }
            cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + months);
        }

        return cal;
    }

    @Override
    public boolean isActivePeriod(int reportPeriodId, long departmentId) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isBalancePeriod(int reportPeriodId, long departmentId) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
