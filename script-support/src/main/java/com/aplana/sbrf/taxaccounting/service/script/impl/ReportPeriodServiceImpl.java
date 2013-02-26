package com.aplana.sbrf.taxaccounting.service.script.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.service.script.ReportPeriodService;


/*
 * Реализация ReportPeriodService
 * @author auldanov
 */
@Service
@Transactional(readOnly = true)
public class ReportPeriodServiceImpl extends AbstractDao implements ReportPeriodService {

	@Autowired
	ReportPeriodDao dao;
	
	@Override
	public ReportPeriod get(int reportPeriodId) {
		return dao.get(reportPeriodId);
	}

	@Override
	public List<ReportPeriod> listByTaxPeriod(int taxPeriodId) {
		return dao.listByTaxPeriod(taxPeriodId);
	}

	@Override
	public ReportPeriod getPrevReportPeriod(int reportPeriodId) {
		ReportPeriod thisReportPeriod= dao.get(reportPeriodId);
		List<ReportPeriod> listByTaxPeriod = dao.listByTaxPeriod(thisReportPeriod.getTaxPeriodId());
		
		for (int i = 0; i < listByTaxPeriod.size(); i++){
			if (listByTaxPeriod.get(i).getTaxPeriodId() == reportPeriodId && i!=0){
				return listByTaxPeriod.get(i-1);
			}
		}
		
		return null;
	}
}
