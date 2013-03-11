package com.aplana.sbrf.taxaccounting.dao.script.impl;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.script.Income102Dao;
import com.aplana.sbrf.taxaccounting.model.Income102;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Реализация Дао для отчета о прибыли и убытках
 */
@Repository("income102Dao")
public class Income102DaoImpl extends AbstractDao implements Income102Dao {

	@Override
	public Income102 getIncome102(int reportPeriodId, String opuCode) {
		return getJdbcTemplate().queryForObject(
			"SELECT * FROM income_102 WHERE REPORT_PERIOD_ID= ? and OPU_CODE = ?",
			new Object[]{reportPeriodId, opuCode},
			new RowMapper<Income102>(){
				@Override
				public Income102 mapRow(ResultSet rs, int rowNum) throws SQLException {
					Income102 income102Data = new Income102();
					income102Data.setReportPeriodId(rs.getInt("REPORT_PERIOD_ID"));
					income102Data.setOpuCode(rs.getString("OPU_CODE"));
					income102Data.setTotalSum(rs.getDouble("TOTAL_SUM"));
					return income102Data;
				}
			}
		);
	}
}
