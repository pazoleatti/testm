package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentParamDao;
import com.aplana.sbrf.taxaccounting.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentParam;
import com.aplana.sbrf.taxaccounting.model.DepartmentType;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

/**
 * DAO для работы с информацией по основным параметрам подразделения банка
 */
@Repository
@Transactional
public class DepartmentParamDaoImpl extends AbstractDao implements DepartmentParamDao {
	public static final RowMapper<DepartmentParam> DEPARTMENT_PARAM_ROW_MAPPER =
		new RowMapper<DepartmentParam>() {
			@Override
			public DepartmentParam mapRow(ResultSet rs, int rowNum)
					throws SQLException {
				DepartmentParam departmentParam = new DepartmentParam();
				departmentParam.setDepartmentId(Integer.valueOf(rs.getInt("department_id")));
				departmentParam.setDictRegionId(rs.getString("dict_region_id"));
				departmentParam.setOkato(rs.getString("okato"));
				departmentParam.setInn(rs.getString("inn"));
				departmentParam.setKpp(rs.getString("kpp"));
				departmentParam.setTaxOrganCode(rs.getString("tax_organ_code"));
				departmentParam.setOkvedCode(rs.getString("okved_code"));
				departmentParam.setPhone(rs.getString("phone"));
				departmentParam.setReorgFormCode(rs.getString("reorg_form_code"));
				departmentParam.setReorgInn(rs.getString("reorg_inn"));
				departmentParam.setReorgKpp(rs.getString("reorg_kpp"));
				return departmentParam;
		}
	};

	@Override
	public DepartmentParam getDepartmentParam(int departmentId) {
		try {
			return getJdbcTemplate().queryForObject(
					"select * from department_param where department_id = ?",
					new Object[] { departmentId },
					new int[] { Types.NUMERIC },
					DEPARTMENT_PARAM_ROW_MAPPER
			);
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException("Не удалось найти подразделение с department_id = %d", departmentId);
		}
	}
}
