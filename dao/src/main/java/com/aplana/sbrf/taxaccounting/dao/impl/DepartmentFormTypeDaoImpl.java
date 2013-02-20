package com.aplana.sbrf.taxaccounting.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.TaxType;

@Repository
@Transactional(readOnly = true)
public class DepartmentFormTypeDaoImpl extends AbstractDao implements DepartmentFormTypeDao {
	private static final RowMapper<DepartmentFormType> DFT_MAPPER = new RowMapper<DepartmentFormType>() {
		@Override
		public DepartmentFormType mapRow(ResultSet rs, int rowNum) throws SQLException {
			DepartmentFormType departmentFormType = new DepartmentFormType();
			departmentFormType.setId(rs.getLong("id"));
			departmentFormType.setFormTypeId(rs.getInt("form_type_id"));
			departmentFormType.setDepartmentId(rs.getInt("department_id"));
			departmentFormType.setKind(FormDataKind.fromId(rs.getInt("kind")));
			return departmentFormType;
		}
	};

	private static final String GET_FORM_SOURCES_SQL = "select * from department_form_type src_dft where exists "
			+ "(select 1 from department_form_type dft, form_data_source fds where "
			+ "fds.department_form_type_id=dft.id and fds.src_department_form_type_id=src_dft.id "
			+ "and dft.department_id=? and dft.form_type_id=? and dft.kind=?)";
	
	@Override
	public List<DepartmentFormType> getFormSources(int departmentId,
			int formTypeId, FormDataKind kind) {
		return getJdbcTemplate().query(
			GET_FORM_SOURCES_SQL,
			new Object[] {
				departmentId,
				formTypeId,
				kind.getId()
			},
			DFT_MAPPER
		);
	}

	private static final String GET_FORM_DESTANATIONS_SQL = "select * from department_form_type dest_dft where exists "
			+ "(select 1 from department_form_type dft, form_data_source fds where "
			+ "fds.src_department_form_type_id=dft.id and fds.department_form_type_id=dest_dft.id "
			+ "and dft.department_id=? and dft.form_type_id=? and dft.kind=?)";
	
	@Override
	public List<DepartmentFormType> getFormDestinations(int sourceDepartmentId,
			int sourceFormTypeId, FormDataKind sourceKind) {
		return getJdbcTemplate().query(
				GET_FORM_DESTANATIONS_SQL,
				new Object[] { sourceDepartmentId, sourceFormTypeId,
						sourceKind.getId() }, DFT_MAPPER);
	}

	private static final String GET_DECLARATION_SOURCES_SQL = "select * from department_form_type src_dft where exists "
			+ "(select 1 from department_declaration_type ddt, declaration_source dds where "
			+ "dds.department_declaration_type_id=ddt.id and dds.src_department_form_type_id=src_dft.id "
			+ "and ddt.department_id=? and ddt.declaration_type_id=?)"; 
	
	@Override
	public List<DepartmentFormType> getDeclarationSources(int departmentId,	int declarationTypeId) {
		return getJdbcTemplate().query(
			GET_DECLARATION_SOURCES_SQL,
			new Object[] {
				departmentId,
				declarationTypeId
			}, 
			DFT_MAPPER
		);
	}
	
	private static final String GET_ALL_DEPARTMENT_SOURCES_SQL = "select * from department_form_type src_dft where "
		+ "exists (select 1 from department_form_type dft, form_data_source fds, form_type src_ft where "
		+ "fds.department_form_type_id=dft.id and fds.src_department_form_type_id=src_dft.id and src_ft.id = src_dft.form_type_id "
		+ "and dft.department_id=? and src_ft.tax_type = ?) "
		+ "or exists (select 1 from department_declaration_type ddt, declaration_source dds, form_type src_ft where "
		+ "dds.department_declaration_type_id=ddt.id and dds.src_department_form_type_id=src_dft.id and src_ft.id = src_dft.form_type_id "
		+ "and ddt.department_id=? and src_ft.tax_type = ?) "; 
	@Override
	public List<DepartmentFormType> getAllDepartmentSources(int departmentId,	TaxType taxType) {
		return getJdbcTemplate().query(
			GET_ALL_DEPARTMENT_SOURCES_SQL,
			new Object[] {
				departmentId,
				String.valueOf(taxType.getCode()),
				departmentId,
				String.valueOf(taxType.getCode())
			},
			DFT_MAPPER
		);
	}

	private final static String GET_SQL = "select * from department_form_type where department_id=?";  
	@Override
	public List<DepartmentFormType> get(int departmentId) {
		return getJdbcTemplate().query(
			GET_SQL, 
			new Object[] {
				departmentId
			},
			DFT_MAPPER
		);
	}
	

	private final static String GET_SQL_BY_TAX_TYPE_SQL = "select * from department_form_type dft where department_id = ?" +
		" and exists (select 1 from form_type ft where ft.id = dft.form_type_id and ft.tax_type = ?)";
	@Override
	public List<DepartmentFormType> getByTaxType(int departmentId, TaxType taxType) {
		return getJdbcTemplate().query(
			GET_SQL_BY_TAX_TYPE_SQL, 
			new Object[] {
				departmentId,
				String.valueOf(taxType.getCode())
			},
			DFT_MAPPER
		);
	}
}
