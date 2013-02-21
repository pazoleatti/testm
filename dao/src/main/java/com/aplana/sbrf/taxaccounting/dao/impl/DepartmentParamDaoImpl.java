package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentParamDao;
import com.aplana.sbrf.taxaccounting.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.DepartmentParam;
import com.aplana.sbrf.taxaccounting.model.DepartmentParamIncome;
import com.aplana.sbrf.taxaccounting.model.DepartmentParamTransport;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

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

	@Override
	public DepartmentParamIncome getDepartmentParamIncome(int departmentId) {
		try {
			return getJdbcTemplate().queryForObject(
					"select * from department_param_income where department_id = ?",
					new Object[] { departmentId },
					new int[] { Types.NUMERIC },
					new RowMapper<DepartmentParamIncome>() {

						@Override
						public DepartmentParamIncome mapRow(ResultSet rs,
								int rowNum) throws SQLException {
							DepartmentParamIncome departmentParamIncome = new DepartmentParamIncome();
							departmentParamIncome.setDepartmentId(rs.getInt("DEPARTMENT_ID"));
							departmentParamIncome.setApproveDocName(rs.getString("APPROVE_DOC_NAME"));
							departmentParamIncome.setApproveOrgName(rs.getString("APPROVE_ORG_NAME"));
							departmentParamIncome.setCorrectionSum(Long.valueOf(rs.getLong("CORRECTION_SUM")));
							departmentParamIncome.setExternalTaxSum(Long.valueOf(rs.getLong("EXTERNAL_TAX_SUM")));
							departmentParamIncome.setSignatoryFirstName(rs.getString("SIGNATORY_FIRSTNAME"));
							departmentParamIncome.setSignatoryId(rs.getInt("SIGNATORY_ID"));
							departmentParamIncome.setSignatoryLastName(rs.getString("SIGNATORY_LASTNAME"));
							departmentParamIncome.setSignatorySurname(rs.getString("SIGNATORY_SURNAME"));
							departmentParamIncome.setTaxPlaceTypeCode(rs.getString("TAX_PLACE_TYPE_CODE"));
							departmentParamIncome.setTaxRate(Long.valueOf(rs.getLong("TAX_RATE")));
							departmentParamIncome.setSumDifference(Long.valueOf(rs.getLong("SUM_DIFFERENCE")));
							return departmentParamIncome;
						}
					}
			);
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException("Не удалось найти транспортный налог с department_id = %d", departmentId);
		}
	}

	@Override
	public DepartmentParamTransport getDepartmentParamTransport(int departmentId) {
		try {
			return getJdbcTemplate().queryForObject(
					"select * from department_param_income where department_id = ?",
					new Object[] { departmentId },
					new int[] { Types.NUMERIC },
					new RowMapper<DepartmentParamTransport>() {

						@Override
						public DepartmentParamTransport mapRow(ResultSet rs,
								int rowNum) throws SQLException {
							DepartmentParamTransport departmentParamTransport = new DepartmentParamTransport();
							departmentParamTransport.setDepartmentId(rs.getInt("DEPARTMENT_ID"));
							departmentParamTransport.setApproveDocName(rs.getString("APPROVE_DOC_NAME"));
							departmentParamTransport.setApproveOrgName(rs.getString("APPROVE_ORG_NAME"));
							departmentParamTransport.setSignatoryFirstname(rs.getString("SIGNATORY_FIRSTNAME"));
							departmentParamTransport.setSignatoryId(rs.getInt("SIGNATORY_ID"));
							departmentParamTransport.setSignatoryLasttname(rs.getString("SIGNATORY_LASTNAME"));
							departmentParamTransport.setSignatorySurname(rs.getString("SIGNATORY_SURNAME"));
							departmentParamTransport.setTaxPlaceTypeCode(rs.getString("TAX_PLACE_TYPE_CODE"));
							
							return departmentParamTransport;
						}
						
					}
					);
			
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException("Не удалось найти налог с department_id = %d", departmentId);
		}
		
	}
}
