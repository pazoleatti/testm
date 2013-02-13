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
public class DepartmentFormTypeDaoImpl extends AbstractDao implements
		DepartmentFormTypeDao {

	public static final String GET_1 = "select * from department_form_type where department_id=?";

	public static final String GET_DFT_TEMPLATE_FT = "select * from department_form_type dft join form_type ft on ft.id=dft.form_type_id where %s";

	/**
	 * Запрос для получения источников форм
	 */
	public static final String GET_SRCDFT_TEMPLATE_DFT$FDS = "select * from department_form_type src_dft where exists "
			+ "(select 1 from department_form_type dft, form_data_source fds where "
			+ "fds.department_form_type_id=dft.id and fds.src_department_form_type_id=src_dft.id "
			+ "and %s)";

	/**
	 * Запрос для получения источников форм с возможностью отбирать и по типу
	 * налога
	 */
	public static final String GET_SRCDFT_TEMPLATE_DFT$FDS$FT = "select * from department_form_type src_dft where exists "
			+ "(select 1 from department_form_type dft, form_data_source fds, form_type ft where "
			+ "fds.department_form_type_id=dft.id and fds.src_department_form_type_id=src_dft.id and dft.form_type_id=ft.id "
			+ "and %s)";

	/**
	 * Запрос для получения источников деклараций
	 */
	public static final String GET_SRCDFT_TEMPLATE_DDT$DDS = "select * from department_form_type src_dft where exists "
			+ "(select 1 from department_declaration_type ddt, declaration_source dds where "
			+ "dds.department_declaration_type_id=ddt.id and dds.src_department_form_type_id=src_dft.id "
			+ "and %s)";

	/**
	 * Запрос для получения назначений форм
	 */
	public static final String GET_DESTDFT_TEMPLATE_DFT$FDS = "select * from department_form_type dest_dft where exists "
			+ "(select 1 from department_form_type dft, form_data_source fds where "
			+ "fds.src_department_form_type_id=dft.id and fds.department_form_type_id=dest_dft.id "
			+ "and %s)";

	public static final String PARAM_1 = "dft.department_id=? and ft.tax_type=?";
	public static final String PARAM_2 = "dft.department_id=? and dft.form_type_id=? and dft.kind=?";
	public static final String PARAM_3 = "ddt.department_id=? and ddt.declaration_type_id=?";
	public static final String PARAM_4 = "dft.department_id=?";

	public static final String GET_FORM_SOURCE_1 = String.format(
			GET_SRCDFT_TEMPLATE_DFT$FDS$FT, PARAM_1);
	public static final String GET_FORM_SOURCE_2 = String.format(
			GET_SRCDFT_TEMPLATE_DFT$FDS, PARAM_2);
	public static final String GET_FORM_SOURCE_3 = String.format(
			GET_SRCDFT_TEMPLATE_DFT$FDS, PARAM_4);
	public static final String GET_FORM_DESTANATIONS = String.format(
			GET_DESTDFT_TEMPLATE_DFT$FDS, PARAM_2);
	public static final String GET_DECL_SOURCES = String.format(
			GET_SRCDFT_TEMPLATE_DDT$DDS, PARAM_3);
	public static final String GET_2 = String.format(GET_DFT_TEMPLATE_FT,
			PARAM_1);


	/**
	 * Запрос для проверки на принадлежность формы к источникам для деклараций
	 * департамента
	 */
	public static final String CHECK_FORM_SOURCES = "select 1 from department_form_type src_dft, form_type src_ft, form src_f, form_data src_fd " +
			"where src_fd.id=? and src_fd.form_id=src_f.id " +
			"and src_f.type_id=src_dft.form_type_id and src_fd.department_id=src_dft.department_id and src_fd.kind=src_dft.kind and (exists "
			+ "(select 1 from department_form_type dft, form_data_source fds where "
			+ "fds.department_form_type_id=dft.id and fds.src_department_form_type_id=src_dft.id "
			+ "and dft.department_id=?) "
			+ "or exists"
			+ "(select 1 from department_declaration_type ddt, declaration_source dds where "
			+ "dds.department_declaration_type_id=ddt.id and dds.src_department_form_type_id=src_dft.id "
			+ "and ddt.department_id=?))";
	
	public static final String CHECK_FORM = "select 1 from department_form_type dft, form f, form_data fd where " +
			"fd.id=? and fd.form_id=f.id " +
			"and f.type_id=dft.form_type_id and fd.department_id=dft.department_id and fd.kind=dft.kind and dft.department_id=?";


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
			TaxType taxType) {
		return getJdbcTemplate().query(GET_FORM_SOURCE_1,
				new Object[] { departmentId, taxType.getCode() }, DFT_MAPPER);
	}

	@Override
	public List<DepartmentFormType> getFormSources(int departmentId,
			int formTypeId, FormDataKind kind) {
		return getJdbcTemplate().query(GET_FORM_SOURCE_2,
				new Object[] { departmentId, formTypeId, kind.getId() },
				DFT_MAPPER);
	}

	@Override
	public List<DepartmentFormType> getFormSources(int departmentId) {
		return getJdbcTemplate().query(GET_FORM_SOURCE_3,
				new Object[] { departmentId }, DFT_MAPPER);
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
	public List<DepartmentFormType> get(int departmentId, TaxType taxType) {
		return getJdbcTemplate().query(GET_2,
				new Object[] { departmentId, taxType.getCode() }, DFT_MAPPER);
	}

	@Override
	public boolean checkFormDataIsSourcesForDepartment(int departmentId, long formDataId) {
		return getJdbcTemplate().queryForList(CHECK_FORM_SOURCES,
				new Object[] { formDataId, departmentId, departmentId },
				Long.class).size() > 0;
	}

	@Override
	public boolean checkFormDataIsDirectForDepartment(int departmentId, long formDataId) {
		return getJdbcTemplate().queryForList(CHECK_FORM,
				new Object[] { formDataId, departmentId },
				Long.class).size() > 0;
	}

}
