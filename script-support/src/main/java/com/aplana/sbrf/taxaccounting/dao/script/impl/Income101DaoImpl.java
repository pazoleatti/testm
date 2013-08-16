package com.aplana.sbrf.taxaccounting.dao.script.impl;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.script.Income101Dao;
import com.aplana.sbrf.taxaccounting.model.Income101;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Реализация Дао для оборотной ведомости
 */
@Repository("income101Dao")
public class Income101DaoImpl extends AbstractDao implements Income101Dao {

	@Override
	public List<Income101> getIncome101(int reportPeriodId, String account) {
		try{
			return getJdbcTemplate().query(
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
                        income101Data.setAccountName(rs.getString("ACCOUNT_NAME"));
                        return income101Data;
                    }
				}
			);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
}
