package com.aplana.sbrf.taxaccounting.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.api.DepartmentDeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.DepartmentDeclarationType;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.TaxType;

/**
 * Реализация DAO для работы с информацией о назначении деклараций подразделениям
 * @author Eugene Stetsenko
 */
@Repository
@Transactional
public class DepartmentDeclarationTypeDaoImpl extends AbstractDao implements DepartmentDeclarationTypeDao {

	public static final RowMapper<DepartmentDeclarationType> DEPARTMENT_DECLARATION_TYPE_ROW_MAPPER =
			new RowMapper<DepartmentDeclarationType>() {

		@Override
		public DepartmentDeclarationType mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			DepartmentDeclarationType departmentDeclarationType = new DepartmentDeclarationType();
			departmentDeclarationType.setId(SqlUtils.getInteger(rs, "id"));
			departmentDeclarationType.setDeclarationTypeId(SqlUtils.getInteger(rs,"declaration_type_id"));
			departmentDeclarationType.setDepartmentId(SqlUtils.getInteger(rs,"department_id"));
			return departmentDeclarationType;
		}
	};

	@Override
	public List<DepartmentDeclarationType> getDepartmentDeclarationTypes(int departmentId) {
		return getJdbcTemplate().query(
				"select * from department_declaration_type where department_id = ?",
				new Object[] { departmentId},
				new int[] {Types.NUMERIC},
				DEPARTMENT_DECLARATION_TYPE_ROW_MAPPER);
	}

	@Override
	public List<DepartmentDeclarationType> getDestinations(int sourceDepartmentId, int sourceFormTypeId, FormDataKind sourceKind) {
		return getJdbcTemplate().query(
				"select * from department_declaration_type src_ddt where exists (" +
						"select 1 from department_form_type dft, declaration_source ds " +
						"where ds.src_department_form_type_id=dft.id and dft.department_id=? and dft.form_type_id=? and dft.kind=? and ds.department_declaration_type_id = src_ddt.id" +
						")",
				new Object[] { sourceDepartmentId, sourceFormTypeId, sourceKind.getId()},
				new int[]{Types.NUMERIC, Types.NUMERIC, Types.NUMERIC},
				DEPARTMENT_DECLARATION_TYPE_ROW_MAPPER);
	}

	@Override
	public Set<Integer> getDepartmentIdsByTaxType(TaxType taxType) {
		Set<Integer> departmentIds = new HashSet<Integer>();
		departmentIds.addAll(getJdbcTemplate().queryForList(
				"select department_id from department_declaration_type ddt where exists " +
						"(select 1 from declaration_type dtype where ddt.declaration_type_id = dtype.id and dtype.tax_type = ?)",
				new Object[]{taxType.getCode()},
				new int[]{Types.VARCHAR},
				Integer.class
		));
		return departmentIds;
	}

	@Override
	public List<DepartmentDeclarationType> getByTaxType(int departmentId, TaxType taxType) {
		return getJdbcTemplate().query(
                "select * from department_declaration_type ddt where department_id = ?" +
                " and exists (select 1 from declaration_type dt where dt.id = ddt.declaration_type_id " +
                        (taxType != null ? "and dt.tax_type in " +SqlUtils.transformTaxTypeToSqlInStatement(Arrays.asList(taxType)) : "") + ")",
				new Object[] {
						departmentId
				},
				DEPARTMENT_DECLARATION_TYPE_ROW_MAPPER
		);
	}

	@Override
	public void save(int departmentId, int declarationTypeId) {
		try {
	        getJdbcTemplate().update(
	                "insert into department_declaration_type (id, department_id, declaration_type_id) " +
	                        " values (SEQ_DEPT_DECLARATION_TYPE.nextval, ?, ?)",
	                new Object[]{ departmentId, declarationTypeId});
		} catch (DataIntegrityViolationException e) {
    		throw new DaoException("Декларация указанного типа уже назначена подразделению", e);
    	}
		
	}

	@Override
	public void delete(Long id) {
		try{
	        getJdbcTemplate().update(
	                "delete from department_declaration_type where id = ?",
	                new Object[]{id}
	        );
		} catch (DataIntegrityViolationException e){
			throw new DaoException("Назначение является приемником данных для форм", e);
		}
	}
}
