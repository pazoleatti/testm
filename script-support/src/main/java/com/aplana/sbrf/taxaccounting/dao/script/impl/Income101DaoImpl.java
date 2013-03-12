package com.aplana.sbrf.taxaccounting.dao.script.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.aplana.sbrf.taxaccounting.exception.DaoException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.script.Income101Dao;
import com.aplana.sbrf.taxaccounting.model.Income101;

/**
 * Реализация Дао для оборотной ведомости
 */
@Repository("income101Dao")
public class Income101DaoImpl extends AbstractDao implements Income101Dao {

	@Override
	public Income101 getIncome101(int reportPeriodId, String account) {
		try{
			return getJdbcTemplate().queryForObject(
					"SELECT * FROM income_101 WHERE REPORT_PERIOD_ID= ? and ACCOUNT = ?",
					new Object[]{reportPeriodId, account},
				new RowMapper<Income101>(){
					@Override
					public Income101 mapRow(ResultSet rs, int rowNum) throws SQLException {
						Income101 income101Data = new Income101();
						income101Data.setReportPeriodId(rs.getInt("REPORT_PERIOD_ID"));
						income101Data.setAccount(rs.getString("ACCOUNT"));
						income101Data.setIncomeDebetRemains(rs.getDouble("INCOME_DEBET_REMAINS"));
						income101Data.setIncomeCreditRemains(rs.getDouble("INCOME_CREDIT_REMAINS"));
						income101Data.setDebetRate(rs.getDouble("DEBET_RATE"));
						income101Data.setCreditRate(rs.getDouble("CREDIT_RATE"));
						income101Data.setOutcomeCreditRemains(rs.getDouble("OUTCOME_CREDIT_REMAINS"));
						income101Data.setOutcomeDebetRemains(rs.getDouble("OUTCOME_DEBET_REMAINS"));
						return income101Data;
					}
				}
			);
		} catch (EmptyResultDataAccessException e) {
			return null;
		} catch (IncorrectResultSizeDataAccessException e) {
			throw new DaoException("Must be one instance of \"101 account form\" for \"reportPeriodId\" and \"account\" params");
		}
	}
}
