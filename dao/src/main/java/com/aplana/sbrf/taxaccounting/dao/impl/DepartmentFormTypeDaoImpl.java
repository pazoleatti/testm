package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
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
            departmentFormType.setId(SqlUtils.getLong(rs,"id"));
            departmentFormType.setFormTypeId(SqlUtils.getInteger(rs,"form_type_id"));
            departmentFormType.setDepartmentId(SqlUtils.getInteger(rs,"department_id"));
            departmentFormType.setKind(FormDataKind.fromId(SqlUtils.getInteger(rs,"kind")));
            departmentFormType.setPerformerId(rs.getInt("performer_dep_id"));
            return departmentFormType;
        }
    };

    private static final RowMapper<Pair<DepartmentFormType, Date>> DFT_SOURCES_MAPPER = new RowMapper<Pair<DepartmentFormType, Date>>() {

        @Override
        public Pair<DepartmentFormType, Date> mapRow(ResultSet rs, int i) throws SQLException {
            DepartmentFormType departmentFormType = new DepartmentFormType();
            departmentFormType.setId(rs.getInt("id"));
            departmentFormType.setFormTypeId(SqlUtils.getInteger(rs,"form_type_id"));
            departmentFormType.setDepartmentId(SqlUtils.getInteger(rs,"department_id"));
            departmentFormType.setKind(FormDataKind.fromId(SqlUtils.getInteger(rs,"kind")));
            return new Pair<DepartmentFormType, Date>(departmentFormType, rs.getDate("start_date"));
        }
    };

    private static final RowMapper<Pair<DepartmentFormType, DepartmentFormType>> DFT_MAPPER_PAIR = new RowMapper<Pair<DepartmentFormType, DepartmentFormType>>() {
        @Override
        public Pair<DepartmentFormType, DepartmentFormType> mapRow(ResultSet rs, int rowNum) throws SQLException {
            DepartmentFormType departmentFTSource = new DepartmentFormType();
            departmentFTSource.setId(SqlUtils.getLong(rs, "source_dft_id"));
            departmentFTSource.setFormTypeId(SqlUtils.getInteger(rs, "src_ft_id"));
            departmentFTSource.setDepartmentId(SqlUtils.getInteger(rs, "src_department_id"));
            departmentFTSource.setKind(FormDataKind.fromId(SqlUtils.getInteger(rs, "src_kind")));
            departmentFTSource.setPerformerId(rs.getInt("src_performer_dep_id"));

            DepartmentFormType departmentFTDest = new DepartmentFormType();
            departmentFTDest.setId(SqlUtils.getLong(rs, "tgt_dft_id"));
            departmentFTDest.setFormTypeId(SqlUtils.getInteger(rs, "tgt_ft_id"));
            departmentFTDest.setDepartmentId(SqlUtils.getInteger(rs, "tgt_department_id"));
            departmentFTDest.setKind(FormDataKind.fromId(SqlUtils.getInteger(rs, "tgt_kind")));
            departmentFTDest.setPerformerId(rs.getInt("tgt_performer_dep_id"));

            return new Pair<DepartmentFormType, DepartmentFormType>(departmentFTSource, departmentFTDest);
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
            departmentFormType.setId(SqlUtils.getInteger(rs,"id"));
            departmentFormType.setDepartmentId(SqlUtils.getInteger(rs,"department_id"));
            departmentFormType.setDeclarationTypeId(SqlUtils.getInteger(rs,"declaration_type_id"));
            return departmentFormType;
        }
    };

    private static final RowMapper<Pair<DepartmentFormType, DepartmentDeclarationType>> DDT_MAPPER_PAIR = new RowMapper<Pair<DepartmentFormType, DepartmentDeclarationType>>() {
        @Override
        public Pair<DepartmentFormType, DepartmentDeclarationType> mapRow(ResultSet rs, int rowNum) throws SQLException {
            DepartmentDeclarationType departmentDTDest = new DepartmentDeclarationType();
            departmentDTDest.setId(SqlUtils.getInteger(rs, "tgt_ddt_id"));
            departmentDTDest.setDepartmentId(SqlUtils.getInteger(rs, "tgt_department_id"));
            departmentDTDest.setDeclarationTypeId(SqlUtils.getInteger(rs, "tgt_dt_id"));

            DepartmentFormType departmentFTSource = new DepartmentFormType();
            departmentFTSource.setId(SqlUtils.getLong(rs, "source_dft_id"));
            departmentFTSource.setDepartmentId(SqlUtils.getInteger(rs, "src_department_id"));
            departmentFTSource.setFormTypeId(SqlUtils.getInteger(rs, "src_ft_id"));
            departmentFTSource.setKind(FormDataKind.fromId(SqlUtils.getInteger(rs, "src_kind")));
            departmentFTSource.setPerformerId(rs.getInt("src_performer_dep_id"));
            return new Pair<DepartmentFormType, DepartmentDeclarationType>(departmentFTSource, departmentDTDest);
        }
    };

    private static final String GET_FORM_SOURCES_SQL_OLD = "select * from department_form_type src_dft where exists "
            + "(select 1 from department_form_type dft, form_data_source fds where "
            + "fds.department_form_type_id=dft.id and fds.src_department_form_type_id=src_dft.id "
            + "and dft.department_id=? %s %s)";

    @Override
    public List<DepartmentFormType> getFormSources(int departmentId,
                                                   int formTypeId, FormDataKind kind) {
        String[] strings = new String[2];
        Object[] params = new Object[]{departmentId};
        if (formTypeId != 0) {
            strings[0] = " and dft.form_type_id= " + formTypeId;
        } else {
            strings[0] = "";
        }
        if (kind != null){
            strings[1] = " and dft.kind = " + kind.getId();
        } else {
            strings[1] = "";
        }
        return getJdbcTemplate().query(
                String.format(GET_FORM_SOURCES_SQL_OLD, strings),
                params,
                DFT_MAPPER
        );
    }

    private static final String GET_FORM_SOURCES_SQL = "select distinct src_dft.*, fds.period_start, fds.period_end \n" +
            "from department_form_type src_dft \n" +
            "join form_data_source fds on fds.src_department_form_type_id=src_dft.id \n" +
            "join department_form_type dft on fds.department_form_type_id=dft.id \n" +
            "where dft.department_id=:departmentId and (:formTypeId is null or dft.form_type_id=:formTypeId) and (:formKind is null or dft.kind=:formKind) \n" +
            "and (:periodStart is null or ((fds.period_end >= :periodStart or fds.period_end is null) and (:periodEnd is null or fds.period_start <= :periodEnd)))";

    @Override
    public List<DepartmentFormType> getFormSources(int departmentId, int formTypeId, FormDataKind kind, Date periodStart, Date periodEnd) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("departmentId", departmentId);
        params.put("formTypeId", formTypeId != 0 ? formTypeId : null);
        params.put("formKind", kind != null ? kind.getId() : null);
        params.put("periodStart", periodStart);
        params.put("periodEnd", periodEnd);
        return getNamedParameterJdbcTemplate().query(GET_FORM_SOURCES_SQL, params, DFT_MAPPER_WITH_PERIOD);
    }

    private static final String GET_FORM_DESTINATIONS_SQL = "select distinct dest_dft.*, fds.period_start, fds.period_end from department_form_type dest_dft \n" +
            "join form_data_source fds on fds.department_form_type_id=dest_dft.id\n" +
            "join department_form_type dft on fds.src_department_form_type_id=dft.id\n" +
            "where dft.department_id=:sourceDepartmentId and (:sourceFormTypeId is null or dft.form_type_id=:sourceFormTypeId) and (:sourceKind is null or dft.kind=:sourceKind) \n" +
            "and (:periodStart is null or ((fds.period_end >= :periodStart or fds.period_end is null) and (:periodEnd is null or fds.period_start <= :periodEnd)))";

    @Override
    public List<DepartmentFormType> getFormDestinations(int sourceDepartmentId, int sourceFormTypeId, FormDataKind sourceKind, Date periodStart, Date periodEnd) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("sourceDepartmentId", sourceDepartmentId);
        params.put("sourceFormTypeId", sourceFormTypeId != 0 ? sourceFormTypeId : null);
        params.put("sourceKind", sourceKind != null ? sourceKind.getId() : null);
        params.put("periodStart", periodStart);
        params.put("periodEnd", periodEnd);
        return getNamedParameterJdbcTemplate().query(GET_FORM_DESTINATIONS_SQL, params, DFT_MAPPER_WITH_PERIOD);
    }

    @Override
    public List<DepartmentFormType> getFormDestinations(int sourceDepartmentId,
                                                        int sourceFormTypeId, FormDataKind sourceKind) {
        StringBuilder sb = new StringBuilder("select * from department_form_type dest_dft where exists "
                + "(select 1 from department_form_type dft, form_data_source fds where "
                + "fds.src_department_form_type_id=dft.id and fds.department_form_type_id=dest_dft.id "
                + "and dft.department_id = ? ");
        if (sourceFormTypeId != 0)
            sb.append(" and dft.form_type_id = ").append(sourceFormTypeId);
        if (sourceKind != null)
            sb.append(" and dft.kind = ").append(sourceKind.getId());
        sb.append(")");
        return getJdbcTemplate().query(
                sb.toString(),
                new Object[]{sourceDepartmentId}, DFT_MAPPER);
    }

    private static final String COMMON_WITH = "with tgt_dep_hierarchy(root_id, id, parent_id, name) as (\n" +
            "select connect_by_root id as root_id, id, parent_id, name\n" +
            "from department\n" +
            "start with id = :terrBankId --IN: Подразделение-ТБ, относительно которого надо построить иерархию\n" +
            "connect by parent_id = prior id)";

    private static final String GET_FORM_DESTINATIONS_FOR_DEPARTMENT_SQL =
            "select fds.src_department_form_type_id source_dft_id, src.department_id src_department_id, src.form_type_id src_ft_id, src.kind src_kind, src.performer_dep_id src_performer_dep_id, \n" +
                    "fds.department_form_type_id tgt_dft_id, tgt.department_id tgt_department_id, tgt.form_type_id tgt_ft_id, tgt.kind tgt_kind, tgt.performer_dep_id tgt_performer_dep_id \n" +
            "from department_form_type src\n" +
            "join form_data_source fds on src.id = fds.src_department_form_type_id\n" +
            "join department_form_type tgt on fds.department_form_type_id = tgt.id\n" +
            "join tgt_dep_hierarchy tdp on tdp.id = tgt.department_id\n" +
            "join form_type ft on ft.id = tgt.form_type_id\n" +
            "where src.department_id=:departmentId\n" +
            "      and ft.tax_type in %s";

    @Override
    public List<Pair<DepartmentFormType, DepartmentFormType>> getFormDestinationsWithDepId(final int departmentId, final int terrBankId, List<TaxType> taxTypes) {
        HashMap<String, Object> values = new HashMap<String, Object>(){{
            put("departmentId", departmentId);
            put("terrBankId", terrBankId);
        }};
        String sqlTag = taxTypes != null && !taxTypes.isEmpty() ?
                SqlUtils.transformTaxTypeToSqlInStatement(taxTypes) :
                SqlUtils.transformTaxTypeToSqlInStatement(Arrays.asList(TaxType.values()));
        try {
            return getNamedParameterJdbcTemplate().query(
                    COMMON_WITH + String.format(GET_FORM_DESTINATIONS_FOR_DEPARTMENT_SQL, sqlTag),
                    values, DFT_MAPPER_PAIR);
        } catch (DataAccessException e){
            throw new DaoException("Ошибка пр получении форм-назначений", e);
        }
    }

    private static final String GET_FORM_SOURCES_FOR_DEPARTMENT_SQL =
            "select fds.src_department_form_type_id source_dft_id, src.department_id src_department_id, src.form_type_id src_ft_id, src.kind src_kind, src.performer_dep_id src_performer_dep_id, \n" +
                    "fds.department_form_type_id tgt_dft_id, tgt.department_id tgt_department_id, tgt.form_type_id tgt_ft_id, tgt.kind tgt_kind, tgt.performer_dep_id tgt_performer_dep_id \n" +
            "from department_form_type src\n" +
            "join form_data_source fds on src.id = fds.src_department_form_type_id\n" +
            "join department_form_type tgt on fds.department_form_type_id = tgt.id\n" +
            "join tgt_dep_hierarchy tdp on tdp.id = src.department_id \n" +
            "join form_type ft on ft.id = src.form_type_id \n" +
            "where tgt.department_id=:departmentId \n" +
            "      and ft.tax_type in %s";

    @Override
    public List<Pair<DepartmentFormType, DepartmentFormType>> getFormSourcesWithDepId(final int departmentId, final int terrBankId, List<TaxType> taxTypes) {
        HashMap<String, Object> values = new HashMap<String, Object>(){{
            put("departmentId", departmentId);
            put("terrBankId", terrBankId);
        }};
        try {
            String sqlTag = taxTypes != null && !taxTypes.isEmpty() ?
                    SqlUtils.transformTaxTypeToSqlInStatement(taxTypes) :
                    SqlUtils.transformTaxTypeToSqlInStatement(Arrays.asList(TaxType.values()));
            return getNamedParameterJdbcTemplate().query(
                    COMMON_WITH + String.format(GET_FORM_SOURCES_FOR_DEPARTMENT_SQL, sqlTag),
                    values, DFT_MAPPER_PAIR);
        } catch (DataAccessException e){
            logger.error("", e);
            throw new DaoException("Ошибка при получении НФ назначений", e);
        }
    }

    private static final String GET_DECLARATION_DESTINATIONS_FOR_DEPARTMENT_SQL =
                "select fds.src_department_form_type_id source_dft_id, src.department_id src_department_id, src.form_type_id src_ft_id, src.kind src_kind, src.performer_dep_id src_performer_dep_id, \n" +
                        "fds.department_declaration_type_id tgt_ddt_id, tgt.department_id tgt_department_id, tgt.declaration_type_id tgt_dt_id \n" +
                "from department_form_type src\n" +
                "join declaration_source fds on src.id = fds.src_department_form_type_id\n" +
                "join department_declaration_type tgt on fds.department_declaration_type_id = tgt.id\n" +
                "join tgt_dep_hierarchy tdp on tdp.id = tgt.department_id\n" +
                "join DECLARATION_TYPE ft on ft.id = tgt.declaration_type_id\n" +
                "where src.department_id=5\n" +
                "   and ft.tax_type in %s";

    @Override
    public List<Pair<DepartmentFormType, DepartmentDeclarationType>> getDeclarationDestinationsWithDepId(final int departmentId, final int terrBankId, List<TaxType> taxTypes) {
        HashMap<String, Object> values = new HashMap<String, Object>(){{
            put("departmentId", departmentId);
            put("terrBankId", terrBankId);
        }};
        String sqlTag = taxTypes != null && !taxTypes.isEmpty() ?
                SqlUtils.transformTaxTypeToSqlInStatement(taxTypes) :
                SqlUtils.transformTaxTypeToSqlInStatement(Arrays.asList(TaxType.values()));
        try {
            return getNamedParameterJdbcTemplate().query(
                    COMMON_WITH + String.format(GET_DECLARATION_DESTINATIONS_FOR_DEPARTMENT_SQL, sqlTag),
                    values, DDT_MAPPER_PAIR);
        } catch (DataAccessException e){
            throw new DaoException("Ошибка при получении деклараций назначений", e);
        }
    }

    private static final String GET_DECLARATION_SOURCES_FOR_DEPARTMENT_SQL =
            "select fds.src_department_form_type_id source_dft_id, src.department_id src_department_id, src.form_type_id src_ft_id, src.kind src_kind, src.performer_dep_id src_performer_dep_id, \n" +
                    "fds.department_declaration_type_id tgt_ddt_id, tgt.department_id tgt_department_id, tgt.declaration_type_id tgt_dt_id \n" +
            "from department_form_type src\n" +
            "join declaration_source fds on src.id = fds.src_department_form_type_id\n" +
            "join department_declaration_type tgt on fds.department_declaration_type_id = tgt.id\n" +
            "join tgt_dep_hierarchy tdp on tdp.id = src.department_id \n" +
            "join form_type ft on ft.id = src.form_type_id \n" +
            "where tgt.department_id=:departmentId\n" +
            "      and ft.tax_type in %s";

    @Override
    public List<Pair<DepartmentFormType, DepartmentDeclarationType>> getDeclarationSourcesWithDepId(final int departmentId, final int terrBankId, List<TaxType> taxTypes) {
        HashMap<String, Object> values = new HashMap<String, Object>(){{
            put("departmentId", departmentId);
            put("terrBankId", terrBankId);
        }};
        String sqlTag = taxTypes != null && !taxTypes.isEmpty() ?
                SqlUtils.transformTaxTypeToSqlInStatement(taxTypes) :
                SqlUtils.transformTaxTypeToSqlInStatement(Arrays.asList(TaxType.values()));
        try {
            return getNamedParameterJdbcTemplate().query(
                    COMMON_WITH + String.format(GET_DECLARATION_SOURCES_FOR_DEPARTMENT_SQL, sqlTag),
                    values, DDT_MAPPER_PAIR);
        } catch (DataAccessException e){
            logger.error("Ошибка при получении источников назначений", e);
            throw new DaoException("", e);
        }
    }

    private static final String GET_DECLARATION_DESTINATIONS_SQL = "select distinct dest_ddt.*, ds.period_start, ds.period_end from department_declaration_type dest_ddt \n" +
            "join declaration_source ds on ds.department_declaration_type_id=dest_ddt.id\n" +
            "join department_form_type dft on ds.src_department_form_type_id=dft.id  \n" +
            "where dft.department_id=:sourceDepartmentId and (:sourceFormTypeId is null or dft.form_type_id=:sourceFormTypeId) and (:sourceKind is null or dft.kind=:sourceKind) \n" +
            "and (:periodStart is null or ((ds.period_end >= :periodStart or ds.period_end is null) and (:periodEnd is null or ds.period_start <= :periodEnd)))";

    @Override
    public List<DepartmentDeclarationType> getDeclarationDestinations(int sourceDepartmentId,
                                                                      int sourceFormTypeId, FormDataKind sourceKind, Date periodStart, Date periodEnd) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("sourceDepartmentId", sourceDepartmentId);
        params.put("sourceFormTypeId", sourceFormTypeId != 0 ? sourceFormTypeId : null);
        params.put("sourceKind", sourceKind != null ? sourceKind.getId() : null);
        params.put("periodStart", periodStart);
        params.put("periodEnd", periodEnd);
        return getNamedParameterJdbcTemplate().query(GET_DECLARATION_DESTINATIONS_SQL, params, DDT_MAPPER_WITH_PERIOD);
    }

    private static final String GET_DECLARATION_DESTINATIONS_SQL_OLD = "select * from department_declaration_type dest_ddt where exists "
            + "(select 1 from department_form_type dft, declaration_source ds where "
            + "ds.src_department_form_type_id=dft.id and ds.department_declaration_type_id=dest_ddt.id "
            + "and dft.department_id=? %s %s)";

    @Override
    public List<DepartmentDeclarationType> getDeclarationDestinations(int sourceDepartmentId,
                                                                      int sourceFormTypeId, FormDataKind sourceKind) {
        String[] strings = new String[2];
        Object[] params = new Object[]{sourceDepartmentId};
        if (sourceFormTypeId != 0) {
            strings[0] = " and dft.form_type_id = " + sourceFormTypeId;
        } else {
            strings[0] = "";
        }
        if (sourceKind != null){
            strings[1] = " and dft.kind = " + sourceKind.getId();
        } else {
            strings[1] = "";
        }

        return getJdbcTemplate().query(
                String.format(GET_DECLARATION_DESTINATIONS_SQL_OLD, strings),
               params, DDT_MAPPER);
    }

    private static final String GET_DECLARATION_SOURCES_SQL = "select distinct src_dft.*, dds.period_start, dds.period_end \n" +
            "from department_form_type src_dft \n" +
            "join declaration_source dds on dds.src_department_form_type_id=src_dft.id \n" +
            "join department_declaration_type ddt on dds.department_declaration_type_id = ddt.id \n" +
            "where ddt.department_id=:departmentId and (:declarationTypeId is null or ddt.declaration_type_id = :declarationTypeId) " +
            "and (:periodStart is null or ((dds.period_end >= :periodStart or dds.period_end is null) and (:periodEnd is null or dds.period_start <= :periodEnd)))";

    @Override
    public List<DepartmentFormType> getDeclarationSources(int departmentId, int declarationTypeId, Date periodStart, Date periodEnd) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("departmentId", departmentId);
        params.put("declarationTypeId", declarationTypeId != 0 ? declarationTypeId : null);
        params.put("periodStart", periodStart);
        params.put("periodEnd", periodEnd);
        return getNamedParameterJdbcTemplate().query(GET_DECLARATION_SOURCES_SQL, params, DFT_MAPPER_WITH_PERIOD);
    }

    private static final String GET_DECLARATION_SOURCES_SQL_OLD = "select * from department_form_type src_dft where exists "
            + "(select 1 from department_declaration_type ddt, declaration_source dds where "
            + "dds.department_declaration_type_id=ddt.id and dds.src_department_form_type_id=src_dft.id "
            + "and ddt.department_id=? %s)";

    @Override
    public List<DepartmentFormType> getDeclarationSources(int departmentId, int declarationTypeId) {
        String[] strings = new String[1];
        Object[] params = new Object[]{departmentId};
        if (declarationTypeId != 0) {
            strings[0] = " and ddt.declaration_type_id= " + declarationTypeId;
        } else {
            strings[0] = "";
        }
        return getJdbcTemplate().query(
                String.format(GET_DECLARATION_SOURCES_SQL_OLD, strings),
                params,
                DFT_MAPPER
        );
    }

    private final RowMapper<FormTypeKind> FORM_ASSIGN_MAPPER = new RowMapper<FormTypeKind>() {
        @Override
        public FormTypeKind mapRow(ResultSet rs, int rowNum) throws SQLException {
            FormTypeKind formTypeKind = new FormTypeKind();
            formTypeKind.setId(SqlUtils.getLong(rs,"id"));
            formTypeKind.setKind(FormDataKind.fromId(SqlUtils.getInteger(rs,"kind")));
            formTypeKind.setName(rs.getString("name"));
            formTypeKind.setFormTypeId(SqlUtils.getLong(rs, "typeId"));
            formTypeKind.setDepartment(departmentDao.getDepartment(SqlUtils.getInteger(rs,"department_id")));
            Integer performerId = SqlUtils.getInteger(rs,"performer_id");
            formTypeKind.setPerformer(performerId==null ? null : departmentDao.getDepartment(performerId));
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
            formTypeKind.setId(SqlUtils.getLong(rs, "id"));
            formTypeKind.setName(rs.getString("name"));
            formTypeKind.setFormTypeId(SqlUtils.getLong(rs, "typeId"));
            formTypeKind.setDepartment(departmentDao.getDepartment(SqlUtils.getInteger(rs,"department_id")));
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
            + "and (:periodStart is null or ((fds.period_end >= :periodStart or fds.period_end is null) and (:periodEnd is null or fds.period_start <= :periodEnd))) "
            + "and dft.department_id=:departmentId and (:taxType is null or src_ft.tax_type = :taxType)) "
            + "or exists (select 1 from department_declaration_type ddt, declaration_source dds, form_type src_ft where "
            + "dds.department_declaration_type_id=ddt.id and dds.src_department_form_type_id=src_dft.id and src_ft.id = src_dft.form_type_id "
            + "and ddt.department_id=:departmentId and (:taxType is null or src_ft.tax_type = :taxType)"
            + "and (:periodStart is null or ((dds.period_end >= :periodStart or dds.period_end is null) and (:periodEnd is null or dds.period_start <= :periodEnd))) ) ";

    @Override
    public List<DepartmentFormType> getDepartmentSources(int departmentId, TaxType taxType, Date periodStart, Date periodEnd) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("periodStart", periodStart);
        params.put("periodEnd", periodEnd);
        params.put("departmentId", departmentId);
        params.put("taxType", taxType != null ? String.valueOf(taxType.getCode()) : null);
        return getNamedParameterJdbcTemplate().query(
                GET_ALL_DEPARTMENT_SOURCES_SQL,
                params,
                DFT_MAPPER
        );
    }

    private static final String GET_ALL_DEPARTMENT_SOURCES_SQL_OLD = "select * from department_form_type src_dft where "
            + "exists (select 1 from department_form_type dft, form_data_source fds, form_type src_ft where "
            + "fds.department_form_type_id=dft.id and fds.src_department_form_type_id=src_dft.id and src_ft.id = src_dft.form_type_id "
            + "and dft.department_id=? %s ) "
            + "or exists (select 1 from department_declaration_type ddt, declaration_source dds, form_type src_ft where "
            + "dds.department_declaration_type_id=ddt.id and dds.src_department_form_type_id=src_dft.id and src_ft.id = src_dft.form_type_id "
            + "and ddt.department_id=? %s) ";

    @Override
    public List<DepartmentFormType> getDepartmentSources(int departmentId, TaxType taxType) {
        Object[] params;
        String[] strings;
        if ( taxType != null) {
            params = new Object[]{
                    departmentId,
                    String.valueOf(taxType.getCode()),
                    departmentId,
                    String.valueOf(taxType.getCode())};
            strings = new String[]{ "and src_ft.tax_type = ?", "and src_ft.tax_type = ?"};
        } else {
            params =  new Object[]{
                    departmentId,
                    departmentId};
            strings =  new String[]{"",""};
        }
        return getJdbcTemplate().query(
                String.format(GET_ALL_DEPARTMENT_SOURCES_SQL_OLD, strings),
                params,
                DFT_MAPPER
        );
    }

    private final static String GET_SQL = "select * from department_form_type where department_id=?";

    @Override
    public List<DepartmentFormType> getByDepartment(int departmentId) {
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
            "  left join form_template ftemp on ftemp.type_id = ft.id \n" +
            "  where ft.id = dft.form_type_id and (:taxType is null or ft.tax_type = :taxType) \n" +
            "  and (:periodStart is null or (ftemp.version >= :periodStart and (:periodEnd is null or ftemp.version <= :periodEnd)))\n" +
            ")";

    @Override
    public List<DepartmentFormType> getByTaxType(int departmentId, TaxType taxType, Date periodStart, Date periodEnd) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("departmentId", departmentId);
        params.put("periodStart", periodStart);
        params.put("periodEnd", periodEnd);
        params.put("taxType", taxType != null ? String.valueOf(taxType.getCode()) : null);
        return getNamedParameterJdbcTemplate().query(GET_SQL_BY_TAX_TYPE_SQL, params, DFT_MAPPER);
    }

    private final static String GET_SQL_BY_TAX_TYPE_SQL_OLD = "select * from department_form_type dft where department_id = ?" +
            " and exists (select 1 from form_type ft where ft.id = dft.form_type_id ";

    @Override
    public List<DepartmentFormType> getByTaxType(int departmentId, TaxType taxType) {
        return getJdbcTemplate().query(
                GET_SQL_BY_TAX_TYPE_SQL_OLD +
                        (taxType != null ? "and ft.tax_type in" + SqlUtils.transformTaxTypeToSqlInStatement(Arrays.asList(taxType)) : "")
                + ")",
                new Object[]{
                        departmentId
                },
                DFT_MAPPER
        );
    }

    @Override
    public List<Long> getByPerformerId(int performerDepId, TaxType taxType, List<FormDataKind> kinds) {
        Object[] sqlParams = new Object[kinds.size() + 2];
        int cnt = 0;
        sqlParams[cnt] = performerDepId;
        cnt++;
        for (FormDataKind kind : kinds) {
            sqlParams[cnt] = kind.getId();
            cnt++;
        }
        sqlParams[cnt] = String.valueOf(taxType.getCode());
        return getJdbcTemplate().queryForList(
                "select dft.form_type_id from department_form_type dft where performer_dep_id = ? " +
                        " and dft.kind in (" + SqlUtils.preparePlaceHolders(kinds.size()) + ")" +
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
            "and (:periodStart is null or ((ds.period_end >= :periodStart or ds.period_end is null) and (:periodEnd is null or ds.period_start <= :periodEnd)))";

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

    private static final String FIND_DESTINATIONS_SQL = "SELECT tgt.id id, tgt.DEPARTMENT_ID department_id, tgt.FORM_TYPE_ID form_type_id, tgt.KIND kind, fds.PERIOD_START start_date \n" +
            "from department_form_type src \n" +
            "join form_data_source fds on src.id = fds.src_department_form_type_id\n" +
            "join department_form_type tgt on fds.department_form_type_id = tgt.id \n" +
            "   WHERE src.form_type_id = :formTypeId " +
            "   AND (fds.PERIOD_START BETWEEN :dateFrom AND :dateTo " +
            "   OR fds.PERIOD_END BETWEEN :dateFrom AND :dateTo \n" +
            "   OR (fds.PERIOD_START BETWEEN :dateFrom AND :dateTo \n" +
            "   AND fds.PERIOD_END IS null))";
    @Override
    public List<Pair<DepartmentFormType, Date>> findDestinationsForFormType(int typeId, Date dateFrom, Date dateTo) {
        try {
            HashMap<String, Object> values = new HashMap<String, Object>();
            values.put("formTypeId", typeId);
            values.put("dateFrom", dateFrom);
            values.put("dateTo", dateTo);

            return getNamedParameterJdbcTemplate().query(
                    FIND_DESTINATIONS_SQL,
                    values,
                    DFT_SOURCES_MAPPER);
        } catch (DataAccessException e){
            logger.error("", e);
            throw new DaoException("", e);
        }
    }

    private static final String FIND_SOURCES_SQL = "SELECT src.id id, src.DEPARTMENT_ID department_id, src.FORM_TYPE_ID form_type_id, src.KIND kind, fds.PERIOD_START start_date \n" +
            "from department_form_type src \n" +
            "join form_data_source fds on src.id = fds.src_department_form_type_id\n" +
            "join department_form_type tgt on fds.department_form_type_id = tgt.id \n" +
            "   WHERE tgt.form_type_id = :formTypeId " +
            "   AND (fds.PERIOD_START BETWEEN :dateFrom AND :dateTo " +
            "   OR fds.PERIOD_END BETWEEN :dateFrom AND :dateTo \n" +
            "   OR (fds.PERIOD_START BETWEEN :dateFrom AND :dateTo \n" +
            "   AND fds.PERIOD_END IS null))";
    @Override
    public List<Pair<DepartmentFormType, Date>> findSourcesForFormType(int typeId, Date dateFrom, Date dateTo) {
        try {
            HashMap<String, Object> values = new HashMap<String, Object>();
            values.put("formTypeId", typeId);
            values.put("dateFrom", dateFrom);
            values.put("dateTo", dateTo);

            return getNamedParameterJdbcTemplate().query(
                    FIND_SOURCES_SQL,
                    values,
                    DFT_SOURCES_MAPPER);
        } catch (DataAccessException e){
            logger.error("", e);
            throw new DaoException("", e);
        }
    }

    @Override
    public List<DepartmentFormType> getDFTByFormType(@NotNull Integer formTypeId) {
        try {
            return getJdbcTemplate().query("select id, department_id, form_type_id, kind, performer_dep_id from DEPARTMENT_FORM_TYPE where FORM_TYPE_ID = ?",
                    new Object[]{formTypeId},
                    DFT_MAPPER);
        }catch (DataAccessException e){
            logger.error("Получение назначений НФ", e);
            throw new DaoException("Получение назначений НФ", e);
        }
    }

    @Override
    public void updatePerformer(int id, Integer performerId){
        getJdbcTemplate().update(
            "update department_form_type set performer_dep_id = ? where id = ?",
                performerId, id);
    }
}
