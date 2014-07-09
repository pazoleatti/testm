package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
@Transactional(readOnly = true)
public class DepartmentFormTypeDaoImpl extends AbstractDao implements DepartmentFormTypeDao {

	private static final Log logger = LogFactory.getLog(DepartmentFormTypeDaoImpl.class);

    @Autowired
    DepartmentDao departmentDao;
	
	public static final String DUPLICATE_ERROR = "Налоговая форма указанного типа и вида уже назначена подразделению";

    private static final RowMapper<DepartmentFormType> DFT_MAPPER_WITH_PERIOD = new RowMapper<DepartmentFormType>() {
        @Override
        public DepartmentFormType mapRow(ResultSet rs, int rowNum) throws SQLException {
            DepartmentFormType departmentFormType = new DepartmentFormType();
            departmentFormType.setId(rs.getLong("id"));
            departmentFormType.setFormTypeId(rs.getInt("form_type_id"));
            departmentFormType.setDepartmentId(rs.getInt("department_id"));
            departmentFormType.setKind(FormDataKind.fromId(rs.getInt("kind")));
            departmentFormType.setPeriodStart(rs.getDate("period_start"));
            departmentFormType.setPeriodEnd(rs.getDate("period_end"));
            return departmentFormType;
        }
    };
	
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

    private static final RowMapper<DepartmentDeclarationType> DDT_MAPPER_WITH_PERIOD = new RowMapper<DepartmentDeclarationType>() {
        @Override
        public DepartmentDeclarationType mapRow(ResultSet rs, int rowNum) throws SQLException {
            DepartmentDeclarationType departmentFormType = new DepartmentDeclarationType();
            departmentFormType.setId(rs.getInt("id"));
            departmentFormType.setDepartmentId(rs.getInt("department_id"));
            departmentFormType.setDeclarationTypeId(rs.getInt("declaration_type_id"));
            departmentFormType.setPeriodStart(rs.getDate("period_start"));
            departmentFormType.setPeriodEnd(rs.getDate("period_end"));
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

    private static final String GET_FORM_SOURCES_SQL_OLD = "select * from department_form_type src_dft where exists "
            + "(select 1 from department_form_type dft, form_data_source fds where "
            + "fds.department_form_type_id=dft.id and fds.src_department_form_type_id=src_dft.id "
            + "and dft.department_id=? and dft.form_type_id=? and dft.kind=?)";

    @Override
    public List<DepartmentFormType> getFormSources(int departmentId,
                                                   int formTypeId, FormDataKind kind) {
        return getJdbcTemplate().query(
                GET_FORM_SOURCES_SQL_OLD,
                new Object[]{
                        departmentId,
                        formTypeId,
                        kind.getId()
                },
                DFT_MAPPER
        );
    }

    private static final String GET_FORM_SOURCES_SQL = "select src_dft.*, fds.period_start, fds.period_end \n" +
            "from department_form_type src_dft \n" +
            "join form_data_source fds on fds.src_department_form_type_id=src_dft.id \n" +
            "join department_form_type dft on fds.department_form_type_id=dft.id \n" +
            "where dft.department_id=:departmentId and dft.form_type_id=:formTypeId and dft.kind=:formKind \n" +
            "and (:periodStart is null or ((fds.period_end >= :periodStart or fds.period_end is null) and fds.period_start <= nvl(:periodEnd, '31.12.9999')))";

    @Override
    public List<DepartmentFormType> getFormSources(int departmentId, int formTypeId, FormDataKind kind, Date periodStart, Date periodEnd) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("departmentId", departmentId);
        params.put("formTypeId", formTypeId);
        params.put("formKind", kind.getId());
        params.put("periodStart", periodStart);
        params.put("periodEnd", periodEnd);
        return getNamedParameterJdbcTemplate().query(GET_FORM_SOURCES_SQL, params, DFT_MAPPER_WITH_PERIOD);
    }

    private static final String GET_FORM_DESTINATIONS_SQL = "select dest_dft.*, fds.period_start, fds.period_end from department_form_type dest_dft \n" +
            "join form_data_source fds on fds.department_form_type_id=dest_dft.id\n" +
            "join department_form_type dft on fds.src_department_form_type_id=dft.id\n" +
            "where dft.department_id=:sourceDepartmentId and dft.form_type_id=:sourceFormTypeId and dft.kind=:sourceKind \n" +
            "and (:periodStart is null or ((fds.period_end >= :periodStart or fds.period_end is null) and fds.period_start <= nvl(:periodEnd, '31.12.9999')))";

    @Override
    public List<DepartmentFormType> getFormDestinations(int sourceDepartmentId, int sourceFormTypeId, FormDataKind sourceKind, Date periodStart, Date periodEnd) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("sourceDepartmentId", sourceDepartmentId);
        params.put("sourceFormTypeId", sourceFormTypeId);
        params.put("sourceKind", sourceKind.getId());
        params.put("periodStart", periodStart);
        params.put("periodEnd", periodEnd);
        return getNamedParameterJdbcTemplate().query(GET_FORM_DESTINATIONS_SQL, params, DFT_MAPPER_WITH_PERIOD);
    }

    private static final String GET_FORM_DESTINATIONS_SQL_OLD = "select * from department_form_type dest_dft where exists "
            + "(select 1 from department_form_type dft, form_data_source fds where "
            + "fds.src_department_form_type_id=dft.id and fds.department_form_type_id=dest_dft.id "
            + "and dft.department_id=? and dft.form_type_id=? and dft.kind=?)";

    @Override
    public List<DepartmentFormType> getFormDestinations(int sourceDepartmentId,
                                                        int sourceFormTypeId, FormDataKind sourceKind) {
        return getJdbcTemplate().query(
                GET_FORM_DESTINATIONS_SQL_OLD,
                new Object[]{sourceDepartmentId, sourceFormTypeId,
                        sourceKind.getId()}, DFT_MAPPER);
    }

    private static final String GET_DECLARATION_DESTINATIONS_SQL = "select dest_ddt.*, ds.period_start, ds.period_end from department_declaration_type dest_ddt \n" +
            "join declaration_source ds on ds.department_declaration_type_id=dest_ddt.id\n" +
            "join department_form_type dft on ds.src_department_form_type_id=dft.id  \n" +
            "where dft.department_id=:sourceDepartmentId and dft.form_type_id=:sourceFormTypeId and dft.kind=:sourceKind\n" +
            "and (:periodStart is null or ((ds.period_end >= :periodStart or ds.period_end is null) and ds.period_start <= nvl(:periodEnd, '31.12.9999')))";

    @Override
    public List<DepartmentDeclarationType> getDeclarationDestinations(int sourceDepartmentId,
                                                                      int sourceFormTypeId, FormDataKind sourceKind, Date periodStart, Date periodEnd) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("sourceDepartmentId", sourceDepartmentId);
        params.put("sourceFormTypeId", sourceFormTypeId);
        params.put("sourceKind", sourceKind.getId());
        params.put("periodStart", periodStart);
        params.put("periodEnd", periodEnd);
        return getNamedParameterJdbcTemplate().query(GET_DECLARATION_DESTINATIONS_SQL, params, DDT_MAPPER_WITH_PERIOD);
    }

    private static final String GET_DECLARATION_DESTINATIONS_SQL_OLD = "select * from department_declaration_type dest_ddt where exists "
            + "(select 1 from department_form_type dft, declaration_source ds where "
            + "ds.src_department_form_type_id=dft.id and ds.department_declaration_type_id=dest_ddt.id "
            + "and dft.department_id=? and dft.form_type_id=? and dft.kind=?)";

    @Override
    public List<DepartmentDeclarationType> getDeclarationDestinations(int sourceDepartmentId,
                                                                      int sourceFormTypeId, FormDataKind sourceKind) {
        return getJdbcTemplate().query(
                GET_DECLARATION_DESTINATIONS_SQL_OLD,
                new Object[]{sourceDepartmentId, sourceFormTypeId,
                        sourceKind.getId()}, DDT_MAPPER);
    }

    private static final String GET_DECLARATION_SOURCES_SQL_OLD = "select * from department_form_type src_dft where exists "
            + "(select 1 from department_declaration_type ddt, declaration_source dds where "
            + "dds.department_declaration_type_id=ddt.id and dds.src_department_form_type_id=src_dft.id "
            + "and ddt.department_id=? and ddt.declaration_type_id=?)";

    @Override
    public List<DepartmentFormType> getDeclarationSources(int departmentId, int declarationTypeId) {
        return getJdbcTemplate().query(
                GET_DECLARATION_SOURCES_SQL_OLD,
                new Object[]{
                        departmentId,
                        declarationTypeId
                },
                DFT_MAPPER
        );
    }

    private static final String GET_DECLARATION_SOURCES_SQL = "select src_dft.*, dds.period_start, dds.period_end \n" +
            "from department_form_type src_dft \n" +
            "join declaration_source dds on dds.src_department_form_type_id=src_dft.id \n" +
            "join department_declaration_type ddt on dds.department_declaration_type_id = ddt.id \n" +
            "where ddt.department_id=:departmentId and ddt.declaration_type_id=:declarationTypeId " +
            "and (:periodStart is null or ((dds.period_end >= :periodStart or dds.period_end is null) and dds.period_start <= nvl(:periodEnd, '31.12.9999')))";

    @Override
    public List<DepartmentFormType> getDeclarationSources(int departmentId, int declarationTypeId, Date periodStart, Date periodEnd) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("departmentId", departmentId);
        params.put("declarationTypeId", declarationTypeId);
        params.put("periodStart", periodStart);
        params.put("periodEnd", periodEnd);
        return getNamedParameterJdbcTemplate().query(GET_DECLARATION_SOURCES_SQL, params, DFT_MAPPER_WITH_PERIOD);
    }

    private final RowMapper<FormTypeKind> FORM_ASSIGN_MAPPER = new RowMapper<FormTypeKind>() {
        @Override
        public FormTypeKind mapRow(ResultSet rs, int rowNum) throws SQLException {
            FormTypeKind formTypeKind = new FormTypeKind();
            formTypeKind.setId(rs.getLong("id"));
            formTypeKind.setKind(FormDataKind.fromId(rs.getInt("kind")));
            formTypeKind.setName(rs.getString("name"));
            formTypeKind.setFormTypeId(rs.getLong("typeId"));
            formTypeKind.setDepartment(departmentDao.getDepartment(rs.getInt("department_id")));
            Integer performerId = rs.getInt("performer_id");
            if (rs.wasNull()){
                formTypeKind.setPerformer(null);
            } else{
                formTypeKind.setPerformer(departmentDao.getDepartment(performerId));
            }
            return formTypeKind;
        }
    };

    private static final String GET_FORM_ASSIGNED_SQL =
            "select dft.id, dft.kind, tf.name, tf.id as typeId, dft.performer_dep_id as performer_id, dft.department_id " +
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

    private final RowMapper<FormTypeKind> DECLARATION_ASSIGN_MAPPER = new RowMapper<FormTypeKind>() {
        @Override
        public FormTypeKind mapRow(ResultSet rs, int rowNum) throws SQLException {
            FormTypeKind formTypeKind = new FormTypeKind();
            formTypeKind.setId(rs.getLong("id"));
            formTypeKind.setName(rs.getString("name"));
            formTypeKind.setFormTypeId(rs.getLong("typeId"));
            formTypeKind.setDepartment(departmentDao.getDepartment(rs.getInt("department_id")));
            return formTypeKind;
        }
    };

    private static final String GET_DECLARATION_ASSIGNED_SQL =
            " select ddt.id, dt.name, dt.id as typeId, ddt.department_id " +
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
            + "and (:periodStart is null or ((fds.period_end >= :periodStart or fds.period_end is null) and fds.period_start <= nvl(:periodEnd, '31.12.9999'))) "
            + "and dft.department_id=:departmentId and src_ft.tax_type = :taxType) "
            + "or exists (select 1 from department_declaration_type ddt, declaration_source dds, form_type src_ft where "
            + "dds.department_declaration_type_id=ddt.id and dds.src_department_form_type_id=src_dft.id and src_ft.id = src_dft.form_type_id "
            + "and ddt.department_id=:departmentId and src_ft.tax_type = :taxType"
            + "and (:periodStart is null or ((dds.period_end >= :periodStart or dds.period_end is null) and dds.period_start <= nvl(:periodEnd, '31.12.9999'))) ) ";

    @Override
    public List<DepartmentFormType> getDepartmentSources(int departmentId, TaxType taxType, Date periodStart, Date periodEnd) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("periodStart", periodStart);
        params.put("periodEnd", periodEnd);
        params.put("departmentId", departmentId);
        params.put("taxType", String.valueOf(taxType.getCode()));
        return getNamedParameterJdbcTemplate().query(
                GET_ALL_DEPARTMENT_SOURCES_SQL,
                params,
                DFT_MAPPER
        );
    }


    private static final String GET_ALL_DEPARTMENT_SOURCES_SQL_OLD = "select * from department_form_type src_dft where "
            + "exists (select 1 from department_form_type dft, form_data_source fds, form_type src_ft where "
            + "fds.department_form_type_id=dft.id and fds.src_department_form_type_id=src_dft.id and src_ft.id = src_dft.form_type_id "
            + "and dft.department_id=? and src_ft.tax_type = ?) "
            + "or exists (select 1 from department_declaration_type ddt, declaration_source dds, form_type src_ft where "
            + "dds.department_declaration_type_id=ddt.id and dds.src_department_form_type_id=src_dft.id and src_ft.id = src_dft.form_type_id "
            + "and ddt.department_id=? and src_ft.tax_type = ?) ";

    @Override
    public List<DepartmentFormType> getDepartmentSources(int departmentId, TaxType taxType) {
        return getJdbcTemplate().query(
                GET_ALL_DEPARTMENT_SOURCES_SQL_OLD,
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

    private final static String GET_SQL_BY_TAX_TYPE_SQL = "select * from department_form_type dft where department_id = :departmentId and exists (\n" +
            "  select 1 from form_type ft \n" +
            "  join form_template ftemp on ftemp.type_id = ft.id \n" +
            "  where ft.id = dft.form_type_id and ft.tax_type in %s \n" +
            "  and ftemp.version >= :periodStart and (:periodEnd is null or ftemp.version <= :periodEnd)\n" +
            ")";

    @Override
    public List<DepartmentFormType> getByTaxType(int departmentId, TaxType taxType, Date periodStart, Date periodEnd) {
        String sql = String.format(GET_SQL_BY_TAX_TYPE_SQL,
                (taxType != null ? SqlUtils.transformTaxTypeToSqlInStatement(Arrays.asList(taxType)) : SqlUtils.transformTaxTypeToSqlInStatement(Arrays.asList(TaxType.values()))));
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("departmentId", departmentId);
        params.put("periodStart", periodStart);
        params.put("periodEnd", periodEnd);
        return getNamedParameterJdbcTemplate().query(sql, params, DFT_MAPPER);
    }

    private final static String GET_SQL_BY_TAX_TYPE_SQL_OLD = "select * from department_form_type dft where department_id = ?" +
            " and exists (select 1 from form_type ft where ft.id = dft.form_type_id and ft.tax_type in ";

    @Override
    public List<DepartmentFormType> getByTaxType(int departmentId, TaxType taxType) {
        return getJdbcTemplate().query(
                GET_SQL_BY_TAX_TYPE_SQL_OLD +
                        (taxType != null ? SqlUtils.transformTaxTypeToSqlInStatement(Arrays.asList(taxType)) : SqlUtils.transformTaxTypeToSqlInStatement(Arrays.asList(TaxType.values())))
                        + ")",
                new Object[]{
                        departmentId
                },
                DFT_MAPPER
        );
    }

    @Override
    public List<Long> getByPerformerId(int performerDepId, TaxType taxType, List<FormDataKind> kinds) {
        Object[] sqlParams = new Object[kinds.size()  + 1];
        int cnt = 0;
        for (FormDataKind kind : kinds) {
            sqlParams[cnt] = kind.getId();
            cnt++;
        }
        sqlParams[cnt] = performerDepId;
        return getJdbcTemplate().queryForList(
                "select dft.form_type_id from department_form_type dft where performer_dep_id = ? " +
                " and dft.kind in (" + SqlUtils.preparePlaceHolders(kinds.size()) +")" +
                " and exists (select 1 from form_type ft where ft.id = dft.form_type_id and ft.tax_type = ?)",
                Long.class,
                sqlParams
        );
    }


    @Override
    public List<Long> getFormTypeBySource(int performerDepId, TaxType taxType, List<FormDataKind> kinds){
        HashMap<String, Object> values = new HashMap<String, Object>(3);
        values.put("performerDepId", performerDepId);

        ArrayList<Integer> ids = new ArrayList<Integer>();
        if (kinds.isEmpty()){
            for (FormDataKind kind : FormDataKind.values())
                ids.add(kind.getId());
        } else {
            for (FormDataKind kind : kinds)
                ids.add(kind.getId());
        }
        values.put("kinds", ids);

        try {
            return getNamedParameterJdbcTemplate().queryForList("with " +
                    "l1 (dep_id, type, kind) as (select dft.department_id, dft.form_type_id, dft.kind from department_form_type dft where performer_dep_id = :performerDepId " +
                    "  and exists (select 1 from form_type ft where ft.id = dft.form_type_id and ft.tax_type in "
                    + (taxType != null ? SqlUtils.transformTaxTypeToSqlInStatement(Arrays.asList(taxType)) :
                                            SqlUtils.transformTaxTypeToSqlInStatement(Arrays.asList(TaxType.values()))) + ")), " +
                    "l2 (dep_id, type, kind) as (select distinct dft.department_id, dft.form_type_id, dft.kind " +
                    "  from form_data_source fds, department_form_type dft, department_form_type dfts, form_type ft " +
                    "  where fds.department_form_type_id = dft.id and fds.src_department_form_type_id = dfts.id " +
                    "  and ft.id = dft.form_type_id and dft.kind in (:kinds) " +
                    "  and (dfts.department_id, dfts.form_type_id, dfts.kind) in (select * from l1)), " +
                    "l3 (dep_id, type, kind) as (select distinct dft.department_id, dft.form_type_id, dft.kind " +
                    "  from form_data_source fds, department_form_type dft, department_form_type dfts, form_type ft " +
                    "  where fds.department_form_type_id = dft.id and fds.src_department_form_type_id = dfts.id " +
                    "  and ft.id = dft.form_type_id and dfts.kind in (:kinds) " +
                    "  and (dfts.department_id, dfts.form_type_id, dfts.kind) in (select * from l2)) " +
                    "select type from l1 " +
                    "union " +
                    "select type from l2 " +
                    "union " +
                    "select type from l3 ",
                    values, Long.class);
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Long>(0);
        }
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(Long id) {
    	try{
	        getJdbcTemplate().update(
	                "delete from department_form_type where id = ?",
                    id);
    	} catch (DataIntegrityViolationException e){
			logger.error(e.getMessage(), e);
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
                    departmentId, formTypeId, formKindId);
    	} catch (DataIntegrityViolationException e){
			logger.error(e.getMessage(), e);
    		throw new DaoException(DUPLICATE_ERROR, e);
    	} 
    }

    @Override
    @Transactional(readOnly = false)
    public void save(int departmentId, int typeId, int kindId, Integer performerId) {
        try {
            getJdbcTemplate().update(
                    "insert into department_form_type (department_id, form_type_id, id, kind, performer_dep_id) " +
                            " values (?, ?, seq_department_form_type.nextval, ?, ?)",
                    departmentId, typeId, kindId, performerId);
        } catch (DataIntegrityViolationException e){
			logger.error(e.getMessage(), e);
            throw new DaoException(DUPLICATE_ERROR, e);
        }
    }


    private static final String CHECK_EXIST = "select id from department_form_type src_dft where "
            + "department_id = ? and form_type_id= ? and kind = ? ";

    @Override
    public boolean existAssignedForm(int departmentId, int typeId, FormDataKind kind){
        return getJdbcTemplate().queryForList(
                    CHECK_EXIST,
                    new Object[]{
                        departmentId,
                        typeId,
                        kind.getId()
                    },
                    Integer.class
                ).size() > 0;
    }

    private static final String EXIST_ACCEPTED_DESTINATIONS = "select dtype.name as declarationType, d.name as departmentName from declaration_data dd\n" +
            "join declaration_template dt on dt.id = dd.declaration_template_id\n" +
            "join declaration_type dtype on dtype.id = dt.declaration_type_id \n" +
            "join department d on d.id = dd.department_id\n" +
            "join department_declaration_type ddt on (ddt.department_id = d.id and ddt.declaration_type_id = dtype.id)\n" +
            "join declaration_source ds on ds.department_declaration_type_id = ddt.id\n" +
            "join department_form_type dft on dft.id = ds.src_department_form_type_id\n" +
            "where dft.department_id = ? and dft.kind = ? and dft.form_type_id = ? and dd.report_period_id = ? and dd.is_accepted = 1\n" +
            "and (:periodStart is null or ((ds.period_end >= :periodStart or ds.period_end is null) and ds.period_start <= nvl(:periodEnd, '31.12.9999')))";

    @Override
    public List<Pair<String, String>> existAcceptedDestinations(int sourceDepartmentId, int sourceFormTypeId,
                                                                FormDataKind sourceKind, Integer reportPeriodId,
                                                                Date periodStart, Date periodEnd) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("sourceDepartmentId", sourceDepartmentId);
        params.put("sourceKind", sourceKind.getId());
        params.put("sourceFormTypeId", sourceFormTypeId);
        params.put("reportPeriodId", reportPeriodId);
        params.put("periodStart", periodStart);
        params.put("periodEnd", periodEnd);
        return getNamedParameterJdbcTemplate().query(EXIST_ACCEPTED_DESTINATIONS, params,
                new RowMapper<Pair<String, String>>() {
                    @Override
                    public Pair<String, String> mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new Pair<String, String>(rs.getString("declarationType"), rs.getString("departmentName"));
                    }
                }
        );
    }

    @Override
    public List<Pair<String, String>> existAcceptedDestinations(int sourceDepartmentId, int sourceFormTypeId, FormDataKind sourceKind, Integer reportPeriodId) {
        return getJdbcTemplate().query("select dtype.name as declarationType, d.name as departmentName from declaration_data dd\n" +
                "join declaration_template dt on dt.id = dd.declaration_template_id\n" +
                "join declaration_type dtype on dtype.id = dt.declaration_type_id \n" +
                "join department d on d.id = dd.department_id\n" +
                "join department_declaration_type ddt on (ddt.department_id = d.id and ddt.declaration_type_id = dtype.id)\n" +
                "join declaration_source ds on ds.department_declaration_type_id = ddt.id\n" +
                "join department_form_type dft on dft.id = ds.src_department_form_type_id\n" +
                "where dft.department_id = ? and dft.kind = ? and dft.form_type_id = ? and dd.report_period_id = ? and dd.is_accepted = 1", new RowMapper<Pair<String, String>>() {
            @Override
            public Pair<String, String> mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new Pair<String, String>(rs.getString("declarationType"), rs.getString("departmentName"));
            }
        }, sourceDepartmentId, sourceKind.getId(), sourceFormTypeId, reportPeriodId);
    }

    @Override
    public void updatePerformer(int id, Integer performerId){
        getJdbcTemplate().update(
            "update department_form_type set performer_dep_id = ? where id = ?",
                performerId, id);
    }
}
