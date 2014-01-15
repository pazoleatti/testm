package com.aplana.sbrf.taxaccounting.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

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

    @Override
    public List<Integer> getDepartmentsBySourceControl(int userDepartmentId, TaxType taxType) {
        return getDepartmentsBySource(userDepartmentId, taxType, false);
    }

    @Override
    public List<Integer> getDepartmentsBySourceControlNs(int userDepartmentId, TaxType taxType) {
        return getDepartmentsBySource(userDepartmentId, taxType, true);
    }

    /**
     * Поиск подразделений, доступных по иерархии и подразделений доступных по связи приемник-источник для этих подразделений
     * @param userDepartmentId Подразделение пользователя
     * @param taxType Тип налога
     * @param isNs true - для роли "Контролер НС", false для роли "Контролер"
     * @return Список id подразделений
     */
    private List<Integer> getDepartmentsBySource(int userDepartmentId, TaxType taxType, boolean isNs) {
        String recursive = isWithRecursive() ? "recursive" : "";

        String availableDepartmentsSql = isNs ?
                "with " + recursive + " tree1 (id, parent_id, type) as " +
                        "(select id, parent_id, type from department where id = ? " +
                        "union all " +
                        "select d.id, d.parent_id, d.type from department d inner join tree1 t1 on d.id = t1.parent_id " +
                        "where d.type >= 2), tree2 (id, root_id, type) as " +
                        "(select id, id root_id, type from department where type = 2 " +
                        "union all " +
                        "select d.id, t2.root_id, d.type from department d inner join tree2 t2 on d.parent_id = t2.id) " +
                        "select tree2.id from tree1, tree2 where tree1.type = 2 and tree2.root_id = tree1.id"
                :
                "with " + recursive + " tree (id) as " +
                        "(select id from department where id = ? " +
                        "union all " +
                        "select d.id from department d inner join tree t on d.parent_id = t.id) " +
                        "select id from tree";

        try {
            return getJdbcTemplate().queryForList("select id from " +
                    "(select distinct " +
                    "case when t3.c = 0 then av_dep.id else link_dep.id end as id " +
                    "from (" + availableDepartmentsSql +
                    ") av_dep left join ( " +
                    "select distinct ddt.department_id parent_id, dft.department_id id " +
                    "from declaration_source ds, department_form_type dft, department_declaration_type ddt, declaration_type dt " +
                    "where ds.department_declaration_type_id = ddt.id and ds.src_department_form_type_id = dft.id " +
                    "and dt.id = ddt.declaration_type_id and dt.tax_type = ?) link_dep " +
                    "on av_dep.id = link_dep.parent_id, (select 0 as c from dual union all select 1 as c from dual) t3) " +
                    "where id is not null",
                    new Object[]{userDepartmentId, String.valueOf(taxType.getCode())}, Integer.class);
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Integer>(0);
        }
    }
}
