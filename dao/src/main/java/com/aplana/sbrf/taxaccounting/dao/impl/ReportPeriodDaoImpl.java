package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

/**
 * Реализация DAO для работы с {@link ReportPeriod отчётными периодами}
 * @author srybakov
*/
@Repository
@Transactional(readOnly = true)
public class ReportPeriodDaoImpl extends AbstractDao implements ReportPeriodDao {

	private static final Log logger = LogFactory.getLog(ReportPeriodDaoImpl.class);


	private static final class ReportPeriodMapper implements RowMapper<ReportPeriod> {
		@Override
		public ReportPeriod mapRow(ResultSet rs, int index) throws SQLException {
			ReportPeriod reportPeriod = new ReportPeriod();
			reportPeriod.setId(rs.getInt("id"));
			reportPeriod.setName(rs.getString("name"));
			reportPeriod.setActive(rs.getBoolean("is_active"));
			reportPeriod.setMonths(rs.getInt("months"));
			reportPeriod.setTaxPeriodId(rs.getInt("tax_period_id"));
			reportPeriod.setOrder(rs.getInt("ord"));
			reportPeriod.setBalancePeriod(rs.getBoolean("is_balance_period"));

			return reportPeriod;
		}
	}

	@Override
	public ReportPeriod get(int periodId) {
		try {
			return getJdbcTemplate().queryForObject(
					"select * from report_period where id = ?",
					new Object[]{periodId},
					new int[]{Types.NUMERIC},
					new ReportPeriodMapper()
		);
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException("Не существует периода с id=" + periodId);
		}
	}

	@Override
	public ReportPeriod getCurrentPeriod(TaxType taxType) {
		try {
			return getJdbcTemplate().queryForObject(
					"select rp.* from report_period rp join tax_period tp on rp.tax_period_id = tp.id where" +
							" tp.tax_type = ? and rp.is_active = 1 and rp.is_balance_period = 0",
					new Object[]{taxType.getCode()},
					new int[]{Types.VARCHAR},
					new ReportPeriodMapper()
		);
		} catch (EmptyResultDataAccessException e) {
			return null;
		} catch(IncorrectResultSizeDataAccessException e){
			throw new DaoException("Существует несколько открытых периодов по виду налога " + taxType);
		}
	}

	@Override
	public List<ReportPeriod> listByTaxPeriod(int taxPeriodId) {
		return getJdbcTemplate().query(
				"select * from report_period where tax_period_id = ? order by ord",
				new Object[]{taxPeriodId},
				new int[]{Types.NUMERIC},
				new ReportPeriodMapper()
		);
	}

	@Override
	public List<ReportPeriod> listByTaxPeriodAndDepartmentId(int taxPeriodId, long departmentId) {
		return getJdbcTemplate().query(
				"select * from report_period where tax_period_id = ? and department_id = ? order by ord",
				new Object[]{taxPeriodId, departmentId},
				new int[]{Types.NUMERIC, Types.NUMERIC},
				new ReportPeriodMapper()
		);
	}

	@Override
	public void changeActive(int reportPeriodId, boolean active) {
		getJdbcTemplate().update(
				"update report_period set is_active = ? where id = ?",
				new Object[]{active, reportPeriodId},
				new int[]{Types.NUMERIC, Types.NUMERIC}
		);
	}
}
