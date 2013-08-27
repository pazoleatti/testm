package com.aplana.sbrf.taxaccounting.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;

@Repository
@Transactional(readOnly=true)
public class DepartmentReportPeriodDaoImpl extends AbstractDao implements
		DepartmentReportPeriodDao {

	@Autowired
	private ReportPeriodDao reportPeriodDao;

	private final RowMapper<DepartmentReportPeriod> mapper = new RowMapper<DepartmentReportPeriod>() {
		@Override
		public DepartmentReportPeriod mapRow(ResultSet rs, int index)
				throws SQLException {
			DepartmentReportPeriod reportPeriod = new DepartmentReportPeriod();
			reportPeriod.setDepartmentId(rs.getLong("DEPARTMENT_ID"));
			reportPeriod.setReportPeriod(reportPeriodDao.get(rs
					.getInt("REPORT_PERIOD_ID")));
			reportPeriod.setActive(rs.getInt("IS_ACTIVE") == 0 ? false : true);
			reportPeriod.setBalance(rs.getInt("IS_BALANCE_PERIOD") == 0 ? false
					: true);
			return reportPeriod;
		}
	};

	@Override
	public List<DepartmentReportPeriod> getByDepartment(Long departmentId) {
		return getJdbcTemplate()
				.query("select * from DEPARTMENT_REPORT_PERIOD where DEPARTMENT_ID=?",
						new Object[] { departmentId },
						new int[] { Types.NUMERIC},
						mapper);
	}

	@Override
	@Transactional(readOnly=false)
	public void save(DepartmentReportPeriod departmentReportPeriod) {
		getJdbcTemplate()
				.update("insert into DEPARTMENT_REPORT_PERIOD (DEPARTMENT_ID, REPORT_PERIOD_ID, IS_ACTIVE, IS_BALANCE_PERIOD)"
						+ " values (?, ?, ?, ?)",
						departmentReportPeriod.getDepartmentId(),
						departmentReportPeriod.getReportPeriod().getId(),
						departmentReportPeriod.isActive(),
						departmentReportPeriod.isBalance());
	}

	@Override
	@Transactional(readOnly=false)
	public void updateActive(int reportPeriodId, Long departmentId,
			boolean active) {
		getJdbcTemplate()
				.update("update DEPARTMENT_REPORT_PERIOD set IS_ACTIVE=? where REPORT_PERIOD_ID=? and DEPARTMENT_ID=?",
						active ? 1 : 0, reportPeriodId, departmentId);
	}

	@Override
	public DepartmentReportPeriod get(int reportPeriodId, Long departmentId) {
		return DataAccessUtils
				.singleResult(getJdbcTemplate()
						.query("select * from DEPARTMENT_REPORT_PERIOD where REPORT_PERIOD_ID=? and DEPARTMENT_ID=?",
								new Object[] { reportPeriodId, departmentId },
								new int[] { Types.NUMERIC, Types.NUMERIC },
								mapper));
	}

}
