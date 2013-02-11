package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentDeclarationType;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;


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
	public List<DepartmentDeclarationType> getDestanations(int sourceDepartmentId, int sourceFormTypeId, FormDataKind sourceKind) {
		return getJdbcTemplate().query(
				"select * from department_declaration_type src_ddt where exists (" +
						"select 1 from department_form_type dft, declaration_source ds " +
						"where ds.src_department_form_type_id=dft.id and dft.department_id=? and dft.form_type_id=? and dft.kind=? and ds.department_declaration_type_id = src_ddt.id" +
						")",
				new Object[] { sourceDepartmentId, sourceFormTypeId, sourceKind.getId()},
				new int[]{Types.NUMERIC, Types.NUMERIC, Types.NUMERIC},
				DEPARTMENT_DECLARATION_TYPE_ROW_MAPPER);
	}
}
