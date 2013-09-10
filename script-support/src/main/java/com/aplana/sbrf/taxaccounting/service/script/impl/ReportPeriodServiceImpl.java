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
		if (reportPeriodlist.size() > 0 && reportPeriodlist.get(reportPeriodlist.size() - 1).getId() == reportPeriodId){
			List<TaxPeriod> taxPeriodlist = taxPeriodDao.listByTaxType(thisTaxPeriod.getTaxType());
			for (int i = 0; i < taxPeriodlist.size(); i++){
				if (taxPeriodlist.get(i).getId().equals(thisTaxPeriod.getId())){
					// получим список отчетных периодов для данного налогового периода
					reportPeriodlist = reportPeriodDao.listByTaxPeriod(taxPeriodlist.get(i - 1).getId());
					// вернем последний отчетный период
					return reportPeriodlist.get(0);
				}
			}
		}
		// не первый отчетный период в данныом налоговом
		for (int i = 0; i < reportPeriodlist.size() - 1; i++){
			if (reportPeriodlist.get(i).getId() == reportPeriodId) {
				return reportPeriodlist.get(i + 1);
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
    public Calendar getStartDate(int reportPeriodId){
        ReportPeriod reportPeriod = reportPeriodDao.get(reportPeriodId);
        TaxPeriod taxPeriod = reportPeriod.getTaxPeriod();
        // календарь
        Calendar cal = Calendar.getInstance();
        cal.setTime(taxPeriod.getStartDate());

        // для налога на прибыль, периоды вложены в друг дгруга, и начало всегда совпадает
        if (taxPeriod.getTaxType() != TaxType.INCOME){
            // получим отчетные периоды для данного налогового периода
            List<ReportPeriod> reportPeriodList = reportPeriodDao.listByTaxPeriod(reportPeriod.getTaxPeriod().getId());
            // если это последний или первый период в налоговом периоде то возвращаем дату начала налогого периода
            if (reportPeriodList.get(0).getId().equals(reportPeriod.getId()) ||
                    reportPeriodList.get(reportPeriodList.size() - 1).getId().equals(reportPeriod.getId())) {
                return cal;
            }
            // второй период, то вернуть 1 апреля
            if (reportPeriodList.get(2).getId().equals(reportPeriod.getId())) {
                cal.set(Calendar.MONTH, 3);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                return cal;
            }
            // третий период, то вернуть 1 июля
            if (reportPeriodList.get(1).getId().equals(reportPeriod.getId())) {
                cal.set(Calendar.MONTH, 6);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                return cal;
            }
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
     * <p>Информация о периодах в конфлюенсе
     * <a href="http://conf.aplana.com/pages/viewpage.action?pageId=9600466">Как считать отчетные периоды для разных налогов</a><p/>
     *
     * @param reportPeriodId
     * @return
     */
    public Calendar getEndDate(int reportPeriodId){
        ReportPeriod reportPeriod = reportPeriodDao.get(reportPeriodId);
        TaxPeriod taxPeriod = taxPeriodDao.get(reportPeriod .getTaxPeriod().getId());
        // календарь
        Calendar cal = Calendar.getInstance();
        cal.setTime(taxPeriod.getStartDate());

        // для налога на прибыль, периоды вложены в друг дгруга
        if (taxPeriod.getTaxType() == TaxType.INCOME){
            cal.set(Calendar.MONTH,  reportPeriod.getMonths());
            cal.add(Calendar.DAY_OF_MONTH, -1);
        } else {
            // получим отчетные периоды для данного налогового периода
            List<ReportPeriod> reportPeriodList = reportPeriodDao.listByTaxPeriod(reportPeriod.getTaxPeriod().getId());
            // смещение относительно налогового периода
            int months = 0;
            for (int i = reportPeriodList.size() - 1; i >= 0; i--) {
                ReportPeriod cReportPeriod = reportPeriodList.get(i);
                // если достигли текущего то выходим из цикла
                if (cReportPeriod.getId().equals(reportPeriod.getId())){
                    months += cReportPeriod.getMonths();
                    break;
                }
                // смещение в месяцах
                months += cReportPeriod.getMonths();
            }
            if (months > 12) {
                months = 12;
            }
            cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + months);
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }

        return cal;
    }

    @Override
    public boolean isActivePeriod(int reportPeriodId, long departmentId) {
        return reportPeriodService.isActivePeriod(reportPeriodId, departmentId);  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isBalancePeriod(int reportPeriodId, long departmentId) {
        return reportPeriodService.isBalancePeriod(reportPeriodId, departmentId);
    }
}
