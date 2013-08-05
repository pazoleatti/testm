package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.service.ReportPeriodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ReportPeriodServiceImpl implements ReportPeriodService{

	@Autowired
	private ReportPeriodDao reportPeriodDao;

	@Override
	public ReportPeriod get(int reportPeriodId) {
		return reportPeriodDao.get(reportPeriodId);
	}

	@Override
	public ReportPeriod getCurrentPeriod(TaxType taxType) {
		return reportPeriodDao.getCurrentPeriod(taxType);
	}

	@Override
	public List<ReportPeriod> listByTaxPeriod(int taxPeriodId) {
		return reportPeriodDao.listByTaxPeriod(taxPeriodId);
	}

	@Override
	public List<ReportPeriod> listByTaxPeriodAndDepartment(int taxPeriodId, long departmentId) {
		return reportPeriodDao.listByTaxPeriodAndDepartmentId(taxPeriodId, departmentId);
	}

	@Override
	public void closePeriod(int reportPeriodId) {
		reportPeriodDao.changeActive(reportPeriodId, false);
	}

	@Override
	public void openPeriod(int reportPeriodId) {
		reportPeriodDao.changeActive(reportPeriodId, true);
	}

	@Override
	public int add(ReportPeriod reportPeriod) {
		return reportPeriodDao.add(reportPeriod);
	}

    @Override
    public ReportPeriod getLastReportPeriod(TaxType taxType, long departmentId) {
        return reportPeriodDao.getLastReportPeriod(taxType, departmentId);
    }
}
