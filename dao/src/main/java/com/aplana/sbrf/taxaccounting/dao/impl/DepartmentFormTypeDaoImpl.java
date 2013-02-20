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

	private static final String GET_1 = "select * from department_form_type where department_id=?";

	/**
	 * Запрос для получения источников форм
	 */
	private static final String GET_SRCDFT_TEMPLATE_DFT$FDS = "select * from department_form_type src_dft where exists "
			+ "(select 1 from department_form_type dft, form_data_source fds where "
			+ "fds.department_form_type_id=dft.id and fds.src_department_form_type_id=src_dft.id "
			+ "and %s)";

	/**
	 * Запрос для получения источников деклараций
	 */
	private static final String GET_SRCDFT_TEMPLATE_DDT$DDS = "select * from department_form_type src_dft where exists "
			+ "(select 1 from department_declaration_type ddt, declaration_source dds where "
			+ "dds.department_declaration_type_id=ddt.id and dds.src_department_form_type_id=src_dft.id "
			+ "and %s)";

	/**
	 * Запрос для получения назначений форм
	 */
	private static final String GET_DESTDFT_TEMPLATE_DFT$FDS = "select * from department_form_type dest_dft where exists "
			+ "(select 1 from department_form_type dft, form_data_source fds where "
			+ "fds.src_department_form_type_id=dft.id and fds.department_form_type_id=dest_dft.id "
			+ "and %s)";

	private static final String PARAM_2 = "dft.department_id=? and dft.form_type_id=? and dft.kind=?";
	private static final String PARAM_3 = "ddt.department_id=? and ddt.declaration_type_id=?";

	private static final String GET_FORM_SOURCE_2 = String.format(
			GET_SRCDFT_TEMPLATE_DFT$FDS, PARAM_2);
	private static final String GET_FORM_DESTANATIONS = String.format(
			GET_DESTDFT_TEMPLATE_DFT$FDS, PARAM_2);
	private static final String GET_DECL_SOURCES = String.format(
			GET_SRCDFT_TEMPLATE_DDT$DDS, PARAM_3);

	public static final RowMapper<DepartmentFormType> DFT_MAPPER = new RowMapper<DepartmentFormType>() {

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
	public List<DepartmentFormType> getFormSources(int departmentId,
			int formTypeId, FormDataKind kind) {
		return getJdbcTemplate().query(GET_FORM_SOURCE_2,
				new Object[] { departmentId, formTypeId, kind.getId() },
				DFT_MAPPER);
	}

	@Override
	public List<DepartmentFormType> getFormDestinations(int sourceDepartmentId,
			int sourceFormTypeId, FormDataKind sourceKind) {
		return getJdbcTemplate().query(
				GET_FORM_DESTANATIONS,
				new Object[] { sourceDepartmentId, sourceFormTypeId,
						sourceKind.getId() }, DFT_MAPPER);
	}

	@Override
	public List<DepartmentFormType> getDeclarationSources(int departmentId,
			int declarationTypeId) {
		return getJdbcTemplate().query(GET_DECL_SOURCES,
				new Object[] { departmentId, declarationTypeId }, DFT_MAPPER);
	}

	@Override
	public List<DepartmentFormType> get(int departmentId) {
		return getJdbcTemplate().query(GET_1, new Object[] { departmentId },
				DFT_MAPPER);
	}

	@Override
	public List<DepartmentFormType> getAllSources(int departmentId,	TaxType taxType) {
		throw new UnsupportedOperationException("not implemented");
	}
}
