package com.aplana.sbrf.taxaccounting.service.script.impl;

import com.aplana.sbrf.taxaccounting.dao.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.service.script.ReportPeriodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
			for (int i = 0; i < taxPeriodlist.size(); i++){
				if (taxPeriodlist.get(i).getId() == thisTaxPeriod.getId() && i != 0){
					// получим список отчетных периодов для данного налогового периода
					reportPeriodlist = reportPeriodDao.listByTaxPeriod(taxPeriodlist.get(i-1).getId());
					// вернем последний отчетный период
					return reportPeriodlist.get(reportPeriodlist.size()-1);
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
}
