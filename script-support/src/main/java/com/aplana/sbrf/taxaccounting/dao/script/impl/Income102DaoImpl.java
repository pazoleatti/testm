package com.aplana.sbrf.taxaccounting.dao.script.impl;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.script.Income102Dao;
import com.aplana.sbrf.taxaccounting.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.Income102;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Реализация Дао для отчета о прибыли и убытках
 */
@Repository("income102Dao")
public class Income102DaoImpl extends AbstractDao implements Income102Dao {

	@Override
	public Income102 getIncome102(int reportPeriodId, String opuCode, int departmentId) {
		try {
			return getJdbcTemplate().queryForObject(
					"SELECT * FROM income_102 WHERE REPORT_PERIOD_ID= ? and OPU_CODE = ? and DEPARTMENT_ID = ?",
					new Object[]{reportPeriodId, opuCode, departmentId},
					new RowMapper<Income102>(){
						@Override
						public Income102 mapRow(ResultSet rs, int rowNum) throws SQLException {
							Income102 income102Data = new Income102();
							income102Data.setReportPeriodId(rs.getInt("REPORT_PERIOD_ID"));
							income102Data.setOpuCode(rs.getString("OPU_CODE"));
                            income102Data.setTotalSum(rs.getDouble("TOTAL_SUM"));
                            income102Data.setDepartmentId(rs.getInt("DEPARTMENT_ID"));
							return income102Data;
						}
					}
				);
		} catch (EmptyResultDataAccessException e) {
			return null;
		} catch (IncorrectResultSizeDataAccessException e) {
			throw new DaoException("Must be one instance of \"102 account form\" for \"reportPeriodId\" and \"opuCode\" params");
		}
	}
}
