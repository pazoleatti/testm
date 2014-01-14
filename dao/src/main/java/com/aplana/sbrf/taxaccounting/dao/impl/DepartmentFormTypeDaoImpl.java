package com.aplana.sbrf.taxaccounting.dao.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.DepartmentDeclarationType;
import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.FormTypeKind;
import com.aplana.sbrf.taxaccounting.model.TaxType;

@Repository
@Transactional(readOnly = true)
public class DepartmentFormTypeDaoImpl extends AbstractDao implements DepartmentFormTypeDao {
	
	public static final String DUBLICATE_ERROR = "Налоговая форма указанного типа и вида уже назначена подразделению";
	
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

    private static final RowMapper<DepartmentDeclarationType> DDT_MAPPER = new RowMapper<DepartmentDeclarationType>() {
        @Override
        public DepartmentDeclarationType mapRow(ResultSet rs, int rowNum) throws SQLException {
            DepartmentDeclarationType departmentFormType = new DepartmentDeclarationType();
            departmentFormType.setId(rs.getInt("id"));
            departmentFormType.setDepartmentId(rs.getInt("department_id"));
            departmentFormType.setDeclarationTypeId(rs.getInt("declaration_type_id"));
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
                new Object[]{
                        departmentId,
                        formTypeId,
                        kind.getId()
                },
                DFT_MAPPER
        );
    }

    @Override
    public void saveFormSources(final Long departmentFormTypeId, final List<Long> sourceDepartmentFormTypeIds) {
        JdbcTemplate jt = getJdbcTemplate();
        jt.update("delete from form_data_source where department_form_type_id = ?",
                new Object[]{departmentFormTypeId});

        BatchPreparedStatementSetter bpss = new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int orderIndex)
                    throws SQLException {
                ps.setLong(1, departmentFormTypeId);
                ps.setLong(2, sourceDepartmentFormTypeIds.get(orderIndex));
            }

            @Override
            public int getBatchSize() {
                return sourceDepartmentFormTypeIds.size();
            }
        };

        jt.batchUpdate(
                "insert into form_data_source (department_form_type_id, src_department_form_type_id) values (?, ?)", bpss);
    }

    private static final String GET_FORM_DESTINATIONS_SQL = "select * from department_form_type dest_dft where exists "
            + "(select 1 from department_form_type dft, form_data_source fds where "
            + "fds.src_department_form_type_id=dft.id and fds.department_form_type_id=dest_dft.id "
            + "and dft.department_id=? and dft.form_type_id=? and dft.kind=?)";

    @Override
    public List<DepartmentFormType> getFormDestinations(int sourceDepartmentId,
                                                        int sourceFormTypeId, FormDataKind sourceKind) {
        return getJdbcTemplate().query(
                GET_FORM_DESTINATIONS_SQL,
                new Object[]{sourceDepartmentId, sourceFormTypeId,
                        sourceKind.getId()}, DFT_MAPPER);
    }

    private static final String GET_DECLARATION_DESTINATIONS_SQL = "select * from department_declaration_type dest_ddt where exists "
            + "(select 1 from department_form_type dft, declaration_source ds where "
            + "ds.src_department_form_type_id=dft.id and ds.department_declaration_type_id=dest_ddt.id "
            + "and dft.department_id=? and dft.form_type_id=? and dft.kind=?)";

    @Override
    public List<DepartmentDeclarationType> getDeclarationDestinations(int sourceDepartmentId,
                                                                      int sourceFormTypeId, FormDataKind sourceKind) {
        return getJdbcTemplate().query(
                GET_DECLARATION_DESTINATIONS_SQL,
                new Object[]{sourceDepartmentId, sourceFormTypeId,
                        sourceKind.getId()}, DDT_MAPPER);
    }

    private static final String GET_DECLARATION_SOURCES_SQL = "select * from department_form_type src_dft where exists "
            + "(select 1 from department_declaration_type ddt, declaration_source dds where "
            + "dds.department_declaration_type_id=ddt.id and dds.src_department_form_type_id=src_dft.id "
            + "and ddt.department_id=? and ddt.declaration_type_id=?)";

    @Override
    public List<DepartmentFormType> getDeclarationSources(int departmentId, int declarationTypeId) {
        return getJdbcTemplate().query(
                GET_DECLARATION_SOURCES_SQL,
                new Object[]{
                        departmentId,
                        declarationTypeId
                },
                DFT_MAPPER
        );
    }

    @Override
    public void saveDeclarationSources(final Long declarationTypeId, final List<Long> sourceDepartmentFormTypeIds) {
        JdbcTemplate jt = getJdbcTemplate();
        jt.update("delete from declaration_source where department_declaration_type_id = ?",
                new Object[]{declarationTypeId});

        BatchPreparedStatementSetter bpss = new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int orderIndex)
                    throws SQLException {
                ps.setLong(1, declarationTypeId);
                ps.setLong(2, sourceDepartmentFormTypeIds.get(orderIndex));
            }

            @Override
            public int getBatchSize() {
                return sourceDepartmentFormTypeIds.size();
            }
        };

        jt.batchUpdate(
                "insert into declaration_source (department_declaration_type_id, src_department_form_type_id) values (?, ?)", bpss);
    }

    private static final RowMapper<FormTypeKind> FORM_ASSIGN_MAPPER = new RowMapper<FormTypeKind>() {
        @Override
        public FormTypeKind mapRow(ResultSet rs, int rowNum) throws SQLException {
            FormTypeKind formTypeKind = new FormTypeKind();
            formTypeKind.setId(rs.getLong("id"));
            formTypeKind.setKind(FormDataKind.fromId(rs.getInt("kind")));
            formTypeKind.setName(rs.getString("name"));
            formTypeKind.setFormTypeId(rs.getLong("typeId"));
            return formTypeKind;
        }
    };

    private static final String GET_FORM_ASSIGNED_SQL =
            "select dft.id, dft.kind, tf.name, tf.id as typeId " +
                    " from form_type tf " +
                    " join department_form_type dft on dft.department_id = ? and dft.form_type_id = tf.id " +
                    " where tf.tax_type = ?";

    @Override
    public List<FormTypeKind> getFormAssigned(Long departmentId, char taxType) {
        return getJdbcTemplate().query(
                GET_FORM_ASSIGNED_SQL,
                new Object[]{
                        departmentId,
                        String.valueOf(taxType)
                },
                FORM_ASSIGN_MAPPER
        );
    }

    private static final RowMapper<FormTypeKind> DECLARATION_ASSIGN_MAPPER = new RowMapper<FormTypeKind>() {
        @Override
        public FormTypeKind mapRow(ResultSet rs, int rowNum) throws SQLException {
            FormTypeKind formTypeKind = new FormTypeKind();
            formTypeKind.setId(rs.getLong("id"));
            formTypeKind.setName(rs.getString("name"));
            formTypeKind.setFormTypeId(rs.getLong("typeId"));
            return formTypeKind;
        }
    };

    private static final String GET_DECLARATION_ASSIGNED_SQL =
            " select ddt.id, dt.name, dt.id as typeId " +
                    "    from declaration_type dt" +
                    "    join department_declaration_type ddt on ddt.department_id = ? and ddt.declaration_type_id = dt.id" +
                    "    where dt.tax_type = ?";

    @Override
    public List<FormTypeKind> getDeclarationAssigned(Long departmentId, char taxType) {
        return getJdbcTemplate().query(
                GET_DECLARATION_ASSIGNED_SQL,
                new Object[]{
                        departmentId,
                        String.valueOf(taxType)
                },
                DECLARATION_ASSIGN_MAPPER
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
    public List<DepartmentFormType> getDepartmentSources(int departmentId, TaxType taxType) {
        return getJdbcTemplate().query(
                GET_ALL_DEPARTMENT_SOURCES_SQL,
                new Object[]{
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
                new Object[]{
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
                new Object[]{
                        departmentId,
                        String.valueOf(taxType.getCode())
                },
                DFT_MAPPER
        );
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(Long id) {
    	try{
	        getJdbcTemplate().update(
	                "delete from department_form_type where id = ?",
	                new Object[]{
	                        id
	                }
	        );
    	} catch (DataIntegrityViolationException e){
    		throw new DaoException("Назначение является источником или приемником данных", e);
    	}
    }

    @Override
    @Transactional(readOnly = false)
    public void save(int departmentId, int formKindId, int formTypeId) {
    	try {
	        getJdbcTemplate().update(
	                "insert into department_form_type (department_id, form_type_id, id, kind) " +
	                        " values (?, ?, seq_department_form_type.nextval, ?)",
	                new Object[]{ departmentId, formTypeId, formKindId });
    	} catch (DataIntegrityViolationException e){
    		throw new DaoException("Налоговая форма указанного типа и вида уже назначена подразделению", e);
    	} 
    }

    @Override
    public List<Integer> getDepartmentsByFormDataSource(int userDepartmentId, TaxType taxType) {
        try {
            return getJdbcTemplate().queryForList(
                    "select id from " +
                            "(select distinct " +
                            "case when t.c = 0 then dep2.id else dep3.id end as id " +
                            "from  " +
                            "(select distinct dep1.id, dft.form_type_id " +
                            "from " +
                            "(with " + (isWithRecursive() ? " recursive " : "") + " tree (id) as " +
                            "(select id from department where id = ?" +
                            "union all " +
                            "select d.id from department d inner join tree t on (d.parent_id = t.id)) " +
                            "select id from tree) dep1, department_form_type dft " +
                            "where dft.department_id = dep1.id) dep2 " +
                            "left join (select distinct dftp.form_type_id, dft.department_id as id " +
                            "from form_data_source fds, department_form_type dft, department_form_type dftp, form_type ft " +
                            "where fds.department_form_type_id = dftp.id and fds.src_department_form_type_id = dft.id and ft.id = dftp.form_type_id " +
                            "and ft.tax_type = ?) dep3 " +
                            "on dep2.form_type_id = dep3.form_type_id, (select 0 as c from dual union all select 1 as c from dual) t) " +
                            "where id is not null",
                    new Object[]{userDepartmentId, String.valueOf(taxType.getCode())}, Integer.class);
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Integer>(0);
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
                    "select distinct dft.department_id parent_id, dfts.department_id id " +
                    "from form_data_source fds, department_form_type dft, department_form_type dfts, form_type ft " +
                    "where fds.department_form_type_id = dft.id and fds.src_department_form_type_id = dfts.id " +
                    "and ft.id = dft.form_type_id and ft.tax_type = ?) link_dep " +
                    "on av_dep.id = link_dep.parent_id, (select 0 as c from dual union all select 1 as c from dual) t3) " +
                    "where id is not null",
                    new Object[]{userDepartmentId, String.valueOf(taxType.getCode())}, Integer.class);
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Integer>(0);
        }
    }
}
