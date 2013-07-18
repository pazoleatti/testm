package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.model.*;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
    public void saveForm(Set<Long> deleteIds, Long departmentId, int typeId, int formId) {
        if (deleteIds != null && !deleteIds.isEmpty()) {
            deleteDepartmentFormType(deleteIds);
        } else {
            createDepartmentFormType(departmentId, typeId, formId);
        }
    }

    private void deleteDepartmentFormType(final Set<Long> removedLinks) {
        getJdbcTemplate().batchUpdate(
                "delete from department_form_type where id = ?",
                new BatchPreparedStatementSetter() {

                    private Iterator<Long> iterator = removedLinks.iterator();

                    @Override
                    public void setValues(PreparedStatement ps, int index) throws SQLException {
                        ps.setLong(1, iterator.next());
                    }

                    @Override
                    public int getBatchSize() {
                        return removedLinks.size();
                    }
                }
        );
    }

    private void createDepartmentFormType(Long departmentId, int typeId, int formId) {
        getJdbcTemplate().update(
                "insert into department_form_type (department_id, form_type_id, id, kind) " +
                        " values (?, ?, seq_department_form_type.nextval, ?)",
                new Object[]{
                        departmentId,
                        formId,
                        typeId
                });
    }

    @Override
    @Transactional(readOnly = false)
    public void saveDecl(Set<Long> deleteIds, Long departmentId, int formId) {
        if (deleteIds != null && !deleteIds.isEmpty()) {
            deleteDepartmentDeclType(deleteIds);
        } else {
            createDepartmentDeclType(departmentId, formId);
        }
    }


    private void deleteDepartmentDeclType(final Set<Long> removedLinks) {
        getJdbcTemplate().batchUpdate(
                "delete from department_declaration_type where id = ?",
                new BatchPreparedStatementSetter() {

                    private Iterator<Long> iterator = removedLinks.iterator();

                    @Override
                    public void setValues(PreparedStatement ps, int index) throws SQLException {
                        ps.setLong(1, iterator.next());
                    }

                    @Override
                    public int getBatchSize() {
                        return removedLinks.size();
                    }
                }
        );
    }

    private void createDepartmentDeclType(Long departmentId, int formId) {
        getJdbcTemplate().update(
                "insert into department_declaration_type ( id, department_id, declaration_type_id) " +
                        " values ( SEQ_DEPT_DECLARATION_TYPE.nextval, ?, ?)",
                new Object[]{
                        departmentId,
                        formId
                });
    }

}
