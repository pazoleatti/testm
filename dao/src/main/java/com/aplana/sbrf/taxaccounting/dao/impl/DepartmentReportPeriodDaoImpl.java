package com.aplana.sbrf.taxaccounting.dao.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.aplana.sbrf.taxaccounting.dao.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;

public class DepartmentReportPeriodDaoImpl extends AbstractDao implements DepartmentReportPeriodDao{
	
	@Autowired
	private ReportPeriodDao reportPeriodDao;

	@Override
	public List<DepartmentReportPeriod> getByDepartment(Long departmentId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void save(DepartmentReportPeriod departmentReportPeriod) {
		getJdbcTemplate().update(
				"insert into DEPARTMENT_REPORT_PERIOD (DEPARTMENT_ID, REPORT_PERIOD_ID, IS_ACTIVE, IS_BALANCE_PERIOD)" +
						" values (?, ?, ?, ?)",
				departmentReportPeriod.getDepartmentId(),
				departmentReportPeriod.getReportPeriod().getId(),
				departmentReportPeriod.isActive(),
				departmentReportPeriod.isBalance()
		);
	}

	@Override
	public void updateActive(int reportPeriodId, Long departmentId,	boolean active) {
		getJdbcTemplate().update(
				"update DEPARTMENT_REPORT_PERIOD set IS_ACTIVE=? where REPORT_PERIOD_ID=? and DEPARTMENT_ID=?",
				active ? 1 : 0,
				reportPeriodId,
				departmentId
		);
	}

	@Override
	public DepartmentReportPeriod get(int reportPeriodId, Long departmentId) {
		// TODO Auto-generated method stub
		return null;
	}

}
