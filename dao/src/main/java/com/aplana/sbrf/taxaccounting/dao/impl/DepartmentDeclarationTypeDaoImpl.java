package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentDeclarationType;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;



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

	@Override
	public void save(int departmentId, List<DepartmentDeclarationType> departmentDeclarationTypes) {
		final List<DepartmentDeclarationType> newLinks= new LinkedList<DepartmentDeclarationType>();
		final List<DepartmentDeclarationType> oldLinks = new LinkedList<DepartmentDeclarationType>();
		final Set<Integer> removedLinks = new HashSet<Integer>(getJdbcTemplate().queryForList(
				"select id from department_declaration_type where department_id = ?",
				new Object[]{departmentId},
				new int[]{Types.NUMERIC},
				Integer.class
		));
		for (DepartmentDeclarationType link : departmentDeclarationTypes) {
			if (link.getId() == 0) {
				newLinks.add(link);
			} else {
				oldLinks.add(link);
				removedLinks.remove(link.getId());
			}
		}

		if(!removedLinks.isEmpty()){
			getJdbcTemplate().batchUpdate(
					"delete from department_declaration_type where id = ?",
					new BatchPreparedStatementSetter() {

						@Override
						public void setValues(PreparedStatement ps, int index) throws SQLException {
							ps.setInt(1, iterator.next());
						}

						@Override
						public int getBatchSize() {
							return removedLinks.size();
						}

						private Iterator<Integer> iterator = removedLinks.iterator();
					}
			);
		}

		// create new
		if (!newLinks.isEmpty()) {
			getJdbcTemplate().batchUpdate(
					"insert into department_declaration_type (department_id, declaration_type_id, id) " +
							"values (?, ?, seq_dept_declaration_type.nextval)",
					new BatchPreparedStatementSetter() {
						@Override
						public void setValues(PreparedStatement ps, int index) throws SQLException {
							DepartmentDeclarationType link = newLinks.get(index);
							ps.setInt(1, link.getDepartmentId());
							ps.setInt(2, link.getDeclarationTypeId());
						}

						@Override
						public int getBatchSize() {
							return newLinks.size();
						}
					}
			);
		}
		// update old
		if (!oldLinks.isEmpty()) {
			getJdbcTemplate().batchUpdate(
					"update department_declaration_type set department_id = ?, declaration_type_id = ? " +
							"where id = ?",
					new BatchPreparedStatementSetter() {
						@Override
						public void setValues(PreparedStatement ps, int index) throws SQLException {
							DepartmentDeclarationType links = oldLinks.get(index);
							ps.setInt(1, links.getDepartmentId());
							ps.setInt(2, links.getDeclarationTypeId());
							ps.setLong(3, links.getId());
						}

						@Override
						public int getBatchSize() {
							return oldLinks.size();
						}
					}
			);
		}
	}
}
