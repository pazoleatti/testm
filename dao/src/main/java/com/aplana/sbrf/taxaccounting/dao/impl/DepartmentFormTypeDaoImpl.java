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
@Transactional(readOnly=true)
public class DepartmentFormTypeDaoImpl extends AbstractDao implements
		DepartmentFormTypeDao {

	public static final String GET_SOURCE = "select * from department_form_type where id in "
			+ "(select fds.src_department_form_type_id from department_form_type dft join form_data_source fds on fds.department_form_type_id=dft.id "
			+ "where dft.department_id=? and dft.form_type_id=? and dft.kind=?)";

	public static final String GET_DESTANATIONS = "select * from department_form_type where id in "
			+ "(select fds.department_form_type_id from department_form_type dft join form_data_source fds on fds.src_department_form_type_id=dft.id "
			+ "where dft.department_id=? and dft.form_type_id=? and dft.kind=?)";

	public static final String GET_SOURCE_DEP = "select distinct department_id from department_form_type where id in "
			+ "(select fds.src_department_form_type_id from department_form_type dft join form_data_source fds on fds.department_form_type_id=dft.id "
			+ "join form_type ft on dft.form_type_id=ft.id where dft.department_id=? and ft.tax_type=?)";

	public static final RowMapper<DepartmentFormType> DEPARTMENTFORMTYPE_MAPPER = new RowMapper<DepartmentFormType>() {

		@Override
		public DepartmentFormType mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			DepartmentFormType departmentFormType = new DepartmentFormType();
			departmentFormType.setId(rs.getLong("id"));
			departmentFormType.setFormTypeId(rs.getInt("form_type_id"));
			departmentFormType.setDepartmentId(rs.getInt("department_id"));
			departmentFormType.setKind(FormDataKind.fromId(rs.getInt("kind")));
			return departmentFormType;
		}

	};

	@Override
	public List<Integer> getSourceDepartmentIds(int departmentId,
			TaxType taxType) {
		return getJdbcTemplate()
				.queryForList(GET_SOURCE_DEP,
						new Object[] { departmentId, taxType.getCode() },
						Integer.class);
	}

	@Override
	public List<DepartmentFormType> getSources(int departmentId,
			int formTypeId, FormDataKind kind) {
		return getJdbcTemplate().query(GET_SOURCE,
				new Object[] { departmentId, formTypeId, kind.getId() },
				DEPARTMENTFORMTYPE_MAPPER);
	}

	@Override
	public List<DepartmentFormType> getDestanations(int sourceDepartmentId,
			int sourceFormTypeId, FormDataKind sourceKind) {
		return getJdbcTemplate().query(
				GET_DESTANATIONS,
				new Object[] { sourceDepartmentId, sourceFormTypeId,
						sourceKind.getId() }, DEPARTMENTFORMTYPE_MAPPER);
	}

	@Override
	public List<DepartmentFormType> getDepartmentFormTypes(int departmentId) {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public List<DepartmentFormType> getDeclarationSources(int departmentId, int declarationTypeId) {
		// TODO: добавить реализацию
		throw new UnsupportedOperationException("not implemented");
	}
}
