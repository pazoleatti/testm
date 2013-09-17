package com.aplana.sbrf.taxaccounting.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.dao.DataIntegrityViolationException;
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
public class DepartmentDeclarationTypeDaoImpl extends AbstractDao implements DepartmentDeclarationTypeDao {

	public static final RowMapper<DepartmentDeclarationType> DEPARTMENT_DECLARATION_TYPE_ROW_MAPPER =
			new RowMapper<DepartmentDeclarationType>() {

		@Override
		public DepartmentDeclarationType mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			DepartmentDeclarationType departmentDeclarationType = new DepartmentDeclarationType();
			departmentDeclarationType.setId(rs.getInt("id"));
			departmentDeclarationType.setDeclarationTypeId(rs.getInt("declaration_type_id"));
			departmentDeclarationType.setDepartmentId(rs.getInt("department_id"));
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

	private final static String GET_SQL_BY_TAX_TYPE_SQL = "select * from department_declaration_type ddt where department_id = ?" +
			" and exists (select 1 from declaration_type dt where dt.id = ddt.declaration_type_id and dt.tax_type = ?)";
	@Override
	public List<DepartmentDeclarationType> getByTaxType(int departmentId, TaxType taxType) {
		return getJdbcTemplate().query(
				GET_SQL_BY_TAX_TYPE_SQL,
				new Object[] {
						departmentId,
						String.valueOf(taxType.getCode())
				},
				DEPARTMENT_DECLARATION_TYPE_ROW_MAPPER
		);
	}

	@Override
	@Transactional(readOnly = false)
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
    @Transactional(readOnly = false)
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
