package com.aplana.sbrf.taxaccounting.service.script.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.service.script.DepartmentFormTypeService;

@Service
@Transactional(readOnly=true)
public class DepartmentFormTypeServiceImpl extends AbstractDao implements DepartmentFormTypeService {

	public static final String GET_SOURCE = "select * from department_form_type where id in "
			+ "(select fds.src_department_form_type_id from department_form_type dft join form_data_source fds on fds.department_form_type_id=dft.id "
			+ "where dft.department_id=? and dft.form_type_id=? and dft.kind=?)";

	public static final String GET_DESTANATIONS = "select * from department_form_type where id in "
			+ "(select fds.department_form_type_id from department_form_type dft join form_data_source fds on fds.src_department_form_type_id=dft.id "
			+ "where dft.department_id=? and dft.form_type_id=? and dft.kind=?)";

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
	public List<DepartmentFormType> getSources(int departmentId,
			int formTypeId, FormDataKind kind) {
		return getJdbcTemplate().query(GET_SOURCE,
				new Object[] { departmentId, formTypeId, kind.getId() },
				DEPARTMENTFORMTYPE_MAPPER);
	}

	@Override
	public List<DepartmentFormType> getDestinations(int sourceDepartmentId,
			int sourceFormTypeId, FormDataKind sourceKind) {
		return getJdbcTemplate().query(
				GET_DESTANATIONS,
				new Object[] { sourceDepartmentId, sourceFormTypeId,
						sourceKind.getId() }, DEPARTMENTFORMTYPE_MAPPER);
	}
	

}
