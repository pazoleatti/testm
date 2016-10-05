package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentFormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
@Transactional(readOnly = true)
public class DepartmentFormTypeDaoImpl extends AbstractDao implements DepartmentFormTypeDao {

	private static final Log LOG = LogFactory.getLog(DepartmentFormTypeDaoImpl.class);

    @Autowired
    DepartmentDao departmentDao;
	
	public static final String DUPLICATE_ERROR = "Налоговая форма указанного типа и вида уже назначена подразделению";

    private class DFTCallBackHandler implements RowCallbackHandler {
        private List<DepartmentFormType> result;
        private boolean withPeriod;
        private Map<Long, List<Integer>> map = new HashMap<Long, List<Integer>>();

        public DFTCallBackHandler(List<DepartmentFormType> result, boolean withPeriod) {
            this.result = result;
            this.withPeriod = withPeriod;
        }

        @Override
        public void processRow(ResultSet rs) throws SQLException {
            DepartmentFormType departmentFormType = new DepartmentFormType();
            departmentFormType.setId(rs.getLong("id"));
            departmentFormType.setTaxType(TaxType.fromCode(rs.getString("tax_type").charAt(0)));
            departmentFormType.setFormTypeId(rs.getInt("form_type_id"));
            departmentFormType.setDepartmentId(rs.getInt("department_id"));
            departmentFormType.setKind(FormDataKind.fromId(rs.getInt("kind")));
            if (withPeriod) {
                departmentFormType.setPeriodStart(rs.getDate("period_start"));
                departmentFormType.setPeriodEnd(rs.getDate("period_end"));
            }

            Integer performerId = SqlUtils.getInteger(rs, "performer_dep_id");
            if (performerId != null) {
                //Заполняет список исполнителей для назначения
                if (map.containsKey(departmentFormType.getId())) {
                    map.get(departmentFormType.getId()).add(performerId);
                } else {
                    List<Integer> performers = new ArrayList<Integer>();
                    performers.add(performerId);
                    map.put(departmentFormType.getId(), performers);
                    departmentFormType.setPerformers(performers);
                    result.add(departmentFormType);
                }
            } else {
                result.add(departmentFormType);
            }
        }
    }

    private static final RowMapper<Pair<DepartmentFormType, Pair<Date, Date>>> DFT_SOURCES_MAPPER = new RowMapper<Pair<DepartmentFormType, Pair<Date, Date>>>() {

        @Override
        public Pair<DepartmentFormType, Pair<Date, Date>> mapRow(ResultSet rs, int i) throws SQLException {
            DepartmentFormType departmentFormType = new DepartmentFormType();
            departmentFormType.setId(rs.getInt("id"));
            departmentFormType.setFormTypeId(SqlUtils.getInteger(rs,"form_type_id"));
            departmentFormType.setDepartmentId(SqlUtils.getInteger(rs,"department_id"));
            departmentFormType.setKind(FormDataKind.fromId(SqlUtils.getInteger(rs,"kind")));
            departmentFormType.setTaxType(TaxType.fromCode(rs.getString("tax_type").charAt(0)));
            Pair<Date, Date> dates = new Pair<Date, Date>(rs.getDate("start_date"), rs.getDate("end_date"));
            return new Pair<DepartmentFormType, Pair<Date, Date>>(departmentFormType, dates);
        }
    };

    private static final RowMapper<DepartmentDeclarationType> DDT_MAPPER_WITH_PERIOD = new RowMapper<DepartmentDeclarationType>() {
        @Override
        public DepartmentDeclarationType mapRow(ResultSet rs, int rowNum) throws SQLException {
            DepartmentDeclarationType departmentDeclarationType = new DepartmentDeclarationType();
            departmentDeclarationType.setId(rs.getInt("id"));
            departmentDeclarationType.setDepartmentId(rs.getInt("department_id"));
            departmentDeclarationType.setDeclarationTypeId(rs.getInt("declaration_type_id"));
            departmentDeclarationType.setPeriodStart(rs.getDate("period_start"));
            departmentDeclarationType.setPeriodEnd(rs.getDate("period_end"));
            departmentDeclarationType.setTaxType(TaxType.fromCode(rs.getString("tax_type").charAt(0)));
            return departmentDeclarationType;
        }
    };

    private static final RowMapper<DepartmentDeclarationType> DDT_MAPPER = new RowMapper<DepartmentDeclarationType>() {
        @Override
        public DepartmentDeclarationType mapRow(ResultSet rs, int rowNum) throws SQLException {
            DepartmentDeclarationType departmentDeclarationType = new DepartmentDeclarationType();
            departmentDeclarationType.setId(SqlUtils.getInteger(rs,"id"));
            departmentDeclarationType.setDepartmentId(SqlUtils.getInteger(rs,"department_id"));
            departmentDeclarationType.setDeclarationTypeId(SqlUtils.getInteger(rs,"declaration_type_id"));
            departmentDeclarationType.setTaxType(TaxType.fromCode(rs.getString("tax_type").charAt(0)));
            return departmentDeclarationType;
        }
    };

    private static final String GET_FORM_SOURCES_SQL = "select distinct src_dft.*, dftp.performer_dep_id, ds.period_start, ds.period_end, d.NAME, ft.NAME, ft.tax_type \n" +
            "from department_form_type src_dft \n" +
            "join form_data_source ds on ds.src_department_form_type_id=src_dft.id \n" +
            "join department_form_type dft on ds.department_form_type_id=dft.id \n" +
            "JOIN department d ON d.id = src_dft.department_id\n" +
            "JOIN form_type ft ON ft.ID = src_dft.form_type_id\n" +
            "left join department_form_type_performer dftp on dftp.DEPARTMENT_FORM_TYPE_ID = src_dft.id \n" +
            "where dft.department_id=:departmentId and (:formTypeId is null or dft.form_type_id=:formTypeId) and (:formKind is null or dft.kind=:formKind) \n" +
            "and (:periodStart is null or ((ds.period_end >= :periodStart or ds.period_end is null) and (:periodEnd is null or ds.period_start <= :periodEnd)))\n";

    @Override
    public List<DepartmentFormType> getFormSources(int departmentId, int formTypeId, FormDataKind kind, Date periodStart,
                                                   Date periodEnd) {
        QueryParams<SourcesSearchOrdering> queryParams = getSearchOrderingDefaultFilter();
        return getFormSources(departmentId, formTypeId, kind, periodStart, periodEnd, queryParams);
    }

    @Override
    public List<DepartmentFormType> getFormSources(int departmentId, int formTypeId, FormDataKind kind, Date periodStart, Date periodEnd, QueryParams queryParams) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("departmentId", departmentId);
        params.put("formTypeId", formTypeId != 0 ? formTypeId : null);
        params.put("formKind", kind != null ? kind.getId() : null);
        params.put("periodStart", periodStart);
        params.put("periodEnd", periodEnd);
        List<DepartmentFormType> result = new ArrayList<DepartmentFormType>();
        getNamedParameterJdbcTemplate().query(GET_FORM_SOURCES_SQL + getSortingClause(queryParams),
                params, new DFTCallBackHandler(result, true));
        return result;
    }

    private static final String GET_FORM_DESTINATIONS_SQL = "select distinct dest_dft.*, dftp.performer_dep_id, fds.period_start, fds.period_end, ft.tax_type from department_form_type dest_dft \n" +
            "join form_data_source fds on fds.department_form_type_id=dest_dft.id\n" +
            "join department_form_type dft on fds.src_department_form_type_id=dft.id\n" +
            "left join department_form_type_performer dftp on dftp.DEPARTMENT_FORM_TYPE_ID = dest_dft.id \n" +
            "join form_type ft on ft.id = dest_dft.form_type_id \n" +
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
        List<DepartmentFormType> result = new ArrayList<DepartmentFormType>();
        getNamedParameterJdbcTemplate().query(GET_FORM_DESTINATIONS_SQL, params, new DFTCallBackHandler(result, true));
        return result;
    }

    @Override
    public List<DepartmentFormType> getFormDestinations(int sourceDepartmentId,
                                                        int sourceFormTypeId, FormDataKind sourceKind) {
        StringBuilder sb = new StringBuilder("select dest_dft.*, dftp.performer_dep_id from department_form_type dest_dft "+
                "join form_type ft on ft.id = dest_dft.form_type_id \n" +
                "where exists "
                + "(select 1 from department_form_type dft, form_data_source fds where "
                + "fds.src_department_form_type_id=dft.id and fds.department_form_type_id=dest_dft.id "
                + "and dft.department_id = ? ");
        if (sourceFormTypeId != 0)
            sb.append(" and dft.form_type_id = ").append(sourceFormTypeId);
        if (sourceKind != null)
            sb.append(" and dft.kind = ").append(sourceKind.getId());
        sb.append(")");

        List<DepartmentFormType> result = new ArrayList<DepartmentFormType>();
        getJdbcTemplate().query(
                sb.toString(),
                new Object[]{sourceDepartmentId}, new DFTCallBackHandler(result, false));
        return result;
    }


    private static final String GET_DECLARATION_DESTINATIONS_SQL = "select distinct dest_ddt.*, ds.period_start, ds.period_end, dt.tax_type from department_declaration_type dest_ddt \n" +
            "join declaration_source ds on ds.department_declaration_type_id=dest_ddt.id\n" +
            "join department_form_type dft on ds.src_department_form_type_id=dft.id  \n" +
            "join declaration_type dt on dt.id = dest_ddt.declaration_type_id \n" +
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

    private static final String GET_DECLARATION_DESTINATIONS_SQL_OLD = "select * from department_declaration_type dest_ddt "+
            "join declaration_type dt on dt.id = dest_ddt.declaration_type_id \n" +
            "where exists "
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

    private static final String GET_DECLARATION_SOURCES_SQL = "select distinct src_dft.*, dftp.performer_dep_id, ds.period_start, ds.period_end, d.NAME, ft.NAME, src_dft.kind as kind, ft.tax_type \n" +
            "from department_form_type src_dft \n" +
            "left join department_form_type_performer dftp on dftp.DEPARTMENT_FORM_TYPE_ID = src_dft.id \n" +
            "join declaration_source ds on ds.src_department_form_type_id=src_dft.id \n" +
            "join department_declaration_type ddt on ds.department_declaration_type_id = ddt.id \n" +
            "JOIN department d ON d.id = src_dft.department_id\n" +
            "JOIN form_type ft ON ft.ID = src_dft.form_type_id\n" +
            "where ddt.department_id=:departmentId and (:declarationTypeId is null or ddt.declaration_type_id = :declarationTypeId) " +
            "and (:periodStart is null or ((ds.period_end >= :periodStart or ds.period_end is null) and (:periodEnd is null or ds.period_start <= :periodEnd)))";

    @Override
    public List<DepartmentFormType> getDeclarationSources(int departmentId, int declarationTypeId, Date periodStart, Date periodEnd) {
        QueryParams<SourcesSearchOrdering> queryParams = getSearchOrderingDefaultFilter();
        return getDeclarationSources(departmentId, declarationTypeId, periodStart, periodEnd, queryParams);
    }

    @Override
    public List<DepartmentFormType> getDeclarationSources(int departmentId, int declarationTypeId, Date periodStart, Date periodEnd, QueryParams filter) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("departmentId", departmentId);
        params.put("declarationTypeId", declarationTypeId != 0 ? declarationTypeId : null);
        params.put("periodStart", periodStart);
        params.put("periodEnd", periodEnd);
        List<DepartmentFormType> result = new ArrayList<DepartmentFormType>();
        getNamedParameterJdbcTemplate().query(GET_DECLARATION_SOURCES_SQL + getSortingClause(filter),
                params, new DFTCallBackHandler(result, true));
        return result;
    }

    private static final String GET_FORM_ASSIGNED_SQL =
            "select dft.id, dft.kind, tf.name, tf.id as typeId, dftp.performer_dep_id as performer_id, dft.department_id " +
                    " from form_type tf " +
                    " join department_form_type dft on dft.department_id = ? and dft.form_type_id = tf.id " +
                    " left join department_form_type_performer dftp on dftp.DEPARTMENT_FORM_TYPE_ID = dft.id \n" +
                    " where tf.tax_type = ?";

    @Override
    public List<FormTypeKind> getFormAssigned(Long departmentId, char taxType) {
        List<FormTypeKind> result = new ArrayList<FormTypeKind>();
        getJdbcTemplate().query(
                GET_FORM_ASSIGNED_SQL,
                new Object[]{
                        departmentId,
                        String.valueOf(taxType)
                },
                new AssignCallBackHandler(result)
        );
        return result;
    }

    /**
     * Метод составляет запрос используя переданные ей параметры
     *
     * @param departmentIds подразделелния
     * @param taxType тип налога
     * @param queryParams параметры пейджинга, сортировки
     */

    private static final String QUERY =
            "SELECT "+
                    "dft.id,kind,dft.name,dft.type_id,dft.department_id, \n" +
                    "dft.department_name, \n" +
                    "dft.department_parent_id, \n" +
                    "dft.department_type, \n" +
                    "dft.department_short_name, \n" +
                    "dft.department_full_name, \n" +
                    "dft.department_tb_index, \n" +
                    "dft.department_sbrf_code, \n" +
                    "dft.department_region_id, \n" +
                    "dft.department_is_active, \n" +
                    "dft.department_code, \n" +
                    "dft.department_garant_use,\n" +
                    "dft.department_sunr_use,\n" +
                    "-- Для исполнителя\n" +
                    "dp.ID        AS performer_id,\n" +
                    "dp.NAME      AS performer_name,\n" +
                    "dp.PARENT_ID AS performer_parent_id,\n" +
                    "dp.TYPE      AS performer_type,\n" +
                    "dp.SHORTNAME AS performer_short_name,\n" +
                    "dp.TB_INDEX  AS performer_tb_index,\n" +
                    "dp.SBRF_CODE AS performer_sbrf_code,\n" +
                    "dp.REGION_ID AS performer_region_id,\n" +
                    "dp.IS_ACTIVE AS performer_is_active,\n" +
                    "dp.CODE      AS performer_code,\n" +
                    "-- Для сортировки\n" +
                    "dp.NAME  AS performer, \n" +
                    "p.FULL_NAME AS performer_full_name \n" +
                    "FROM (" +
                    "SELECT dftOrd.*,\n" +
                    "    rownum AS row_number_over\n" +
                    "  FROM(\n"+
                    "SELECT dft.ID as id,\n" +
                    "  dft.KIND,\n" +
                    "  ft.NAME,\n" +
                    "  ft.ID AS type_id,\n" +
                    "  -- Для подразделения\n" +
                    "  d.ID         AS department_id,\n" +
                    "  d.NAME       AS department_name,\n" +
                    "  d.PARENT_ID  AS department_parent_id,\n" +
                    "  d.TYPE       AS department_type,\n" +
                    "  d.SHORTNAME  AS department_short_name,\n" +
                    "  d.TB_INDEX   AS department_tb_index,\n" +
                    "  d.SBRF_CODE  AS department_sbrf_code,\n" +
                    "  d.REGION_ID  AS department_region_id,\n" +
                    "  d.IS_ACTIVE  AS department_is_active,\n" +
                    "  d.CODE       AS department_code,\n" +
                    "  d.GARANT_USE AS department_garant_use,\n" +
                    "  d.SUNR_USE   AS department_sunr_use,\n" +
                    "  -- Для сортировки\n" +
                    "  ft.NAME  AS form_type,\n" +
                    "  dft.KIND AS form_kind,\n" +
                    "  d.NAME   AS department,\n" +
                    "  d.FULL_NAME AS department_full_name \n" +
                    "FROM department_form_type dft\n" +
                    "JOIN form_type ft ON ft.ID = dft.FORM_TYPE_ID\n" +
                    "JOIN (SELECT d.*, LTRIM(SYS_CONNECT_BY_PATH(name, '/'), '/') as full_name FROM department d START with parent_id is null CONNECT BY PRIOR id = parent_id) d ON d.ID = dft.DEPARTMENT_ID\n" +
                    "WHERE ft.tax_type = :taxType\n" +
                    "%s\n" +
                    "%s\n" +
                    ") dftOrd \n" +
                    ") dft \n" +
                    "left join department_form_type_performer dftp on 0=:withoutPerformers and dftp.DEPARTMENT_FORM_TYPE_ID = dft.id \n" +
                    "LEFT OUTER JOIN department dp ON dp.ID = dftp.PERFORMER_DEP_ID\n" +
                    "LEFT OUTER JOIN (SELECT d.*, LTRIM(SYS_CONNECT_BY_PATH(name, '/'), '/') as full_name FROM department d START with parent_id is null CONNECT BY PRIOR id = parent_id) p ON p.ID = dftp.PERFORMER_DEP_ID\n";

    private QueryData getAssignedFormsQueryData(List<Long> departmentIds, char taxType, QueryParams<TaxNominationColumnEnum> queryParams, boolean withoutPerformers){
        boolean paging = queryParams != null && queryParams.getCount() != 0;

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("taxType", String.valueOf(taxType));

        // order
        StringBuilder order = new StringBuilder("");
        if (queryParams != null && queryParams.getSearchOrdering() != null) {
            String asc = queryParams.isAscending()?"":" DESC";
            Set<Enum<TaxNominationColumnEnum>> set = new LinkedHashSet<Enum<TaxNominationColumnEnum>>();
            set.add(queryParams.getSearchOrdering());
            set.add(TaxNominationColumnEnum.DEPARTMENT_FULL_NAME);
            set.add(TaxNominationColumnEnum.FORM_KIND);
            set.add(TaxNominationColumnEnum.FORM_TYPE);

            boolean first = true;
            for(Enum<TaxNominationColumnEnum> column: set) {
                if (first)
                    order.append("ORDER BY ");
                else
                    order.append(", ");
                order.append(column.name()).append(asc);
                first = false;
            }
        }

        // departments
        String departmentClause = "";
        if (departmentIds != null && !departmentIds.isEmpty()){
            departmentClause = "AND " + SqlUtils.transformToSqlInStatement("dft.department_id", departmentIds) + "\n";
            parameters.addValue("params", departmentIds);
        }

        String query = String.format(QUERY, departmentClause, order.toString());

        // Limit
        if (paging){
            query = query + " WHERE dft.row_number_over BETWEEN :from and :to";
            parameters.addValue("from", queryParams.getFrom()+1);
            parameters.addValue("to", queryParams.getFrom() + queryParams.getCount());
        }
        parameters.addValue("withoutPerformers", withoutPerformers ? 1 : 0);

        QueryData queryData = new QueryData();
        queryData.setQuery(query);
        queryData.setParameterSource(parameters);

        return queryData;
    }

    private void fillPerformers(Map<FormTypeKind, List<Department>> map, List<FormTypeKind> result, Department performer, FormTypeKind formTypeKind) {
        //Заполняет список исполнителей для назначения
        if (map.containsKey(formTypeKind)) {
            map.get(formTypeKind).add(performer);
        } else {
            List<Department> performers = new ArrayList<Department>();
            performers.add(performer);
            map.put(formTypeKind, performers);
            formTypeKind.setPerformers(performers);
            result.add(formTypeKind);
        }
    }

    private class AssignCallBackHandler implements RowCallbackHandler {
        private List<FormTypeKind> result;
        private Map<FormTypeKind, List<Department>> map = new HashMap<FormTypeKind, List<Department>>();

        public AssignCallBackHandler(List<FormTypeKind> result) {
            this.result = result;
        }

        @Override
        public void processRow(ResultSet rs) throws SQLException {
            FormTypeKind formTypeKind = new FormTypeKind();
            formTypeKind.setId(SqlUtils.getLong(rs,"id"));
            formTypeKind.setKind(FormDataKind.fromId(SqlUtils.getInteger(rs,"kind")));
            formTypeKind.setName(rs.getString("name"));
            formTypeKind.setFormTypeId(SqlUtils.getLong(rs, "typeId"));
            formTypeKind.setDepartment(departmentDao.getDepartment(SqlUtils.getInteger(rs,"department_id")));
            Integer performerId = SqlUtils.getInteger(rs,"performer_id");
            if (performerId != null) {
                Department performer = departmentDao.getDepartment(performerId);
                fillPerformers(map, result, performer, formTypeKind);
            } else {
                result.add(formTypeKind);
            }
        }
    }

    private class AllAssignCallBackHandler implements RowCallbackHandler {
        private List<FormTypeKind> result;
        private Map<FormTypeKind, List<Department>> map = new HashMap<FormTypeKind, List<Department>>();

        public AllAssignCallBackHandler(List<FormTypeKind> result) {
            this.result = result;
        }

        @Override
        public void processRow(ResultSet rs) throws SQLException {
            // Подразделение
            Department department = new Department();
            department.setId(SqlUtils.getInteger(rs, "department_id"));
            department.setName(rs.getString("department_name"));
            Integer departmentParentId = SqlUtils.getInteger(rs, "department_parent_id");
            // В ResultSet есть особенность что если пришло значение нул то вернет значение по умолчанию - то есть для Integer'a вернет 0
            // а так как у нас в базе 0 используется в качестве идентификатора то нужно null нужно првоерять через .wasNull()
            department.setParentId(rs.wasNull() ? null : departmentParentId);
            department.setType(DepartmentType.fromCode(SqlUtils.getInteger(rs, "department_type")));
            department.setShortName(rs.getString("department_short_name"));
            department.setFullName(rs.getString("department_full_name"));
            department.setTbIndex(rs.getString("department_tb_index"));
            department.setSbrfCode(rs.getString("department_sbrf_code"));
            department.setRegionId(SqlUtils.getLong(rs, "department_region_id"));
            if (rs.wasNull()) {
                department.setRegionId(null);
            }
            department.setActive(rs.getBoolean("department_is_active"));
            department.setCode(rs.getLong("department_code"));
            department.setGarantUse(rs.getBoolean("department_garant_use"));
            department.setSunrUse(rs.getBoolean("department_sunr_use"));

            // Исполнитель
            Integer performerId = SqlUtils.getInteger(rs, "performer_id");
            Department performer = new Department();

            FormTypeKind formTypeKind = new FormTypeKind();
            formTypeKind.setId(SqlUtils.getLong(rs, "id"));
            formTypeKind.setKind(FormDataKind.fromId(SqlUtils.getInteger(rs, "kind")));
            formTypeKind.setName(rs.getString("name"));
            formTypeKind.setFormTypeId(SqlUtils.getLong(rs, "type_id"));
            formTypeKind.setDepartment(department);

            if (performerId != null) {
                performer.setId(SqlUtils.getInteger(rs, "performer_id"));
                performer.setName(rs.getString("performer_name"));
                Integer performerParentId = SqlUtils.getInteger(rs, "performer_parent_id");
                // В ResultSet есть особенность что если пришло значение нул то вернет значение по умолчанию - то есть для Integer'a вернет 0
                // а так как у нас в базе 0 используется в качестве идентификатора то нужно null нужно проверять через .wasNull()
                performer.setParentId(rs.wasNull() ? null : performerParentId);
                performer.setType(DepartmentType.fromCode(SqlUtils.getInteger(rs, "performer_type")));
                performer.setShortName(rs.getString("performer_short_name"));
                performer.setFullName(rs.getString("performer_full_name"));
                performer.setTbIndex(rs.getString("performer_tb_index"));
                performer.setSbrfCode(rs.getString("performer_sbrf_code"));
                performer.setRegionId(SqlUtils.getLong(rs, "performer_region_id"));
                if (rs.wasNull()) {
                    performer.setRegionId(null);
                }
                performer.setActive(rs.getBoolean("performer_is_active"));
                performer.setCode(rs.getLong("performer_code"));
                department.setGarantUse(rs.getBoolean("department_garant_use"));
                department.setSunrUse(rs.getBoolean("department_sunr_use"));
                fillPerformers(map, result, performer, formTypeKind);
            } else {
                result.add(formTypeKind);
            }
        }
    }

    @Override
    public List<FormTypeKind> getAllFormAssigned(List<Long> departmentIds, char taxType, QueryParams<TaxNominationColumnEnum> queryParams) {
        QueryData assignedFormsQueryData = getAssignedFormsQueryData(departmentIds, taxType, queryParams, false);
        List<FormTypeKind> result = new ArrayList<FormTypeKind>();
        getNamedParameterJdbcTemplate().query(assignedFormsQueryData.getQuery(), assignedFormsQueryData.getParameterSource(), new AllAssignCallBackHandler(result));
        return result;
    }

    private final RowMapper<FormTypeKind> declarationAssignMapper = new RowMapper<FormTypeKind>() {
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
                declarationAssignMapper
        );
    }

    private static final String GET_ALL_DEPARTMENT_SOURCES_SQL = "select src_dft.id, src_dft.department_id, src_dft.form_type_id, "
			+ "src_dft.kind, dftp.performer_dep_id, ft.tax_type from department_form_type src_dft " +
            "left join department_form_type_performer dftp on dftp.DEPARTMENT_FORM_TYPE_ID = src_dft.id \n" +
            "join form_type ft on ft.id = src_dft.form_type_id \n" +
            "where "
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
        List<DepartmentFormType> result = new ArrayList<DepartmentFormType>();
        getNamedParameterJdbcTemplate().query(
                GET_ALL_DEPARTMENT_SOURCES_SQL,
                params,
                new DFTCallBackHandler(result, false)
        );
        return result;
    }


    private static final String GET_SQL = "SELECT dft.id, dft.department_id, dft.form_type_id, dft.kind, dftp.performer_dep_id, ft.tax_type FROM department_form_type dft " +
            "left join department_form_type_performer dftp on dftp.DEPARTMENT_FORM_TYPE_ID = dft.id \n" +
            "join form_type ft on ft.id = dft.form_type_id \n" +
            "WHERE dft.department_id=?";

    @Override
    public List<DepartmentFormType> getByListIds(List<Long> ids) {
        List<DepartmentFormType> result = new ArrayList<DepartmentFormType>();
        getJdbcTemplate().query(
                "SELECT dft.id, dft.department_id, dft.form_type_id, dft.kind, dftp.performer_dep_id, ft.tax_type FROM department_form_type dft \n" +
                        "left join department_form_type_performer dftp on dftp.DEPARTMENT_FORM_TYPE_ID = dft.id \n" +
                        "join form_type ft on ft.id = dft.form_type_id \n" +
                        "WHERE " +
                        SqlUtils.transformToSqlInStatement("dft.id", ids),
                new DFTCallBackHandler(result, false));
        return result;
    }

    @Override
    public List<DepartmentFormType> getByDepartment(int departmentId) {
        List<DepartmentFormType> result = new ArrayList<DepartmentFormType>();
        getJdbcTemplate().query(
                GET_SQL,
                new Object[]{departmentId},
                new DFTCallBackHandler(result, false)
        );
        return result;
    }

    private static final String GET_SQL_BY_TAX_TYPE_SQL = "select " +
			"src_dft.id, src_dft.department_id, src_dft.form_type_id, src_dft.kind, dftp.performer_dep_id, " +
			"ft.name, ft.tax_type from department_form_type src_dft\n" +
            "left join department_form_type_performer dftp on dftp.DEPARTMENT_FORM_TYPE_ID = src_dft.id \n" +
            "LEFT JOIN form_type ft ON src_dft.FORM_TYPE_ID = ft.ID\n" +
            "where department_id = :departmentId and exists (\n" +
            "select 1 from form_type ft \n" +
            "left join form_template ftemp on ftemp.type_id = ft.id \n" +
            "left join form_template next_ftemp on (next_ftemp.type_id = ft.id and next_ftemp.version > ftemp.version and next_ftemp.status = 0) \n" +
            "where ft.id = src_dft.form_type_id and (:taxType is null or ft.tax_type = :taxType) \n" +
            "and (:periodStart is null or (ftemp.status = 0 and (\n" +
            "(:periodStart >= ftemp.version and (next_ftemp.version is null or :periodStart <= next_ftemp.version)) or \n" +
            "(:periodStart <= ftemp.version and (:periodEnd is null or :periodEnd >= ftemp.version))\n" +
            ")))\n" +
            ")";

    @Override
    public List<DepartmentFormType> getByTaxType(int departmentId, TaxType taxType, Date periodStart, Date periodEnd) {
        QueryParams<SourcesSearchOrdering> queryParams = getSearchOrderingDefaultFilter();
        return getByTaxType(departmentId, taxType, periodStart, periodEnd, queryParams);

    }

    @Override
    public List<DepartmentFormType> getByTaxType(int departmentId, TaxType taxType, Date periodStart, Date periodEnd, QueryParams queryParams) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("departmentId", departmentId);
        params.put("periodStart", periodStart);
        params.put("periodEnd", periodEnd);
        params.put("taxType", taxType != null ? String.valueOf(taxType.getCode()) : null);
        List<DepartmentFormType> result = new ArrayList<DepartmentFormType>();
        getNamedParameterJdbcTemplate().query(GET_SQL_BY_TAX_TYPE_SQL + getSortingClause(queryParams), params, new DFTCallBackHandler(result, false));
        return result;
    }

    private static final String GET_SQL_BY_TAX_TYPE_SQL_OLD =
			"SELECT dft.id, dft.department_id, dft.form_type_id, dft.kind, dftp.performer_dep_id, ft.tax_type FROM department_form_type dft " +
                    "left join department_form_type_performer dftp on dftp.DEPARTMENT_FORM_TYPE_ID = dft.id \n" +
                    "join form_type ft on ft.id = dft.form_type_id \n" +
                    "WHERE dft.department_id = ?" +
            " AND EXISTS (SELECT 1 FROM form_type ft WHERE ft.id = dft.form_type_id ";

    @Override
    public List<DepartmentFormType> getByTaxType(int departmentId, TaxType taxType) {
        List<DepartmentFormType> result = new ArrayList<DepartmentFormType>();
        getJdbcTemplate().query(
                GET_SQL_BY_TAX_TYPE_SQL_OLD +
                        (taxType != null ? "AND ft.tax_type IN " + SqlUtils.transformTaxTypeToSqlInStatement(Arrays.asList(taxType)) : "")
                + ")",
                new Object[]{departmentId},
                new DFTCallBackHandler(result, false)
        );
        return result;
    }

    @Override
    public List<Long> getByPerformerId(int performerDepId, List<TaxType> taxTypes, List<FormDataKind> kinds) {
        try {
            return getJdbcTemplate().queryForList(
                    "select distinct dft.form_type_id from department_form_type dft " +
                            "left join department_form_type_performer dftp on dftp.DEPARTMENT_FORM_TYPE_ID = dft.id \n" +
                            "where dftp.performer_dep_id = ? " +
                            (kinds.isEmpty() ? "" : " and dft.kind in " + SqlUtils.transformFormKindsToSqlInStatement(kinds))+
                            " and exists (select 1 from form_type ft where ft.id = dft.form_type_id " +
                            (taxTypes.isEmpty() ? ")" : " and ft.tax_type in " + SqlUtils.transformTaxTypeToSqlInStatement(taxTypes) + " )"),
                    Long.class,
                    performerDepId);
        } catch (DataAccessException e){
            LOG.error("", e);
            throw new DaoException("", e);
        }
    }

    @Override
    public List<Long> getDFTByPerformerId(int performerDepId, List<TaxType> taxTypes, List<FormDataKind> kinds) {
        try {
            return getJdbcTemplate().queryForList(
                    "select dft.ID from department_form_type dft " +
                            "left join department_form_type_performer dftp on dftp.DEPARTMENT_FORM_TYPE_ID = dft.id \n" +
                            "where dftp.performer_dep_id = ? " +
                            (kinds.isEmpty() ? "" : " and dft.kind in " + SqlUtils.transformFormKindsToSqlInStatement(kinds))+
                            " and exists (select 1 from form_type ft where ft.id = dft.form_type_id " +
                            (taxTypes.isEmpty() ? ")" : " and ft.tax_type in " + SqlUtils.transformTaxTypeToSqlInStatement(taxTypes) + " )"),
                    Long.class,
                    performerDepId);
        } catch (DataAccessException e){
            LOG.error("", e);
            throw new DaoException("", e);
        }
    }


    @Override
    public List<Long> getFormTypeBySource(final int performerDepId, TaxType taxType, List<FormDataKind> kinds){
        final ArrayList<Integer> ids = new ArrayList<Integer>();
        if (kinds.isEmpty()){
            for (FormDataKind kind : FormDataKind.values())
                ids.add(kind.getId());
        } else {
            for (FormDataKind kind : kinds)
                ids.add(kind.getId());
        }

        try {
            return getNamedParameterJdbcTemplate().queryForList("with " +
                    "l1 (dep_id, type, kind) as (select dft.department_id, dft.form_type_id, dft.kind from department_form_type dft " +
                            "left join department_form_type_performer dftp on dftp.DEPARTMENT_FORM_TYPE_ID = dft.id \n" +
                    "where dftp.performer_dep_id = :performerDepId " +
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
					new HashMap<String, Object>(3) {{
						put("performerDepId", performerDepId);
						put("kinds", ids);
					}},
					Long.class);
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
			LOG.error(e.getMessage(), e);
    		throw new DaoException("Назначение является источником или приемником данных", e);
    	}
    }

    @Override
    public void delete(List<Long> ids) {
        try {
            getJdbcTemplate().update("delete from DEPARTMENT_FORM_TYPE where " + SqlUtils.transformToSqlInStatement("id", ids));
        } catch (DataAccessException e){
            LOG.error("", e);
            throw new DaoException("", e);
        }
    }

    @Override
    @Transactional(readOnly = false)
    public long save(int departmentId, int typeId, int kindId) {
        try {
            long id = generateId("seq_department_form_type", Long.class);
            getJdbcTemplate().update(
                    "insert into department_form_type (id, department_id, form_type_id, kind) " +
                            " values (?, ?, ?, ?)",
                    id, departmentId, typeId, kindId);
            return id;
        } catch (DataIntegrityViolationException e){
			LOG.error(e.getMessage(), e);
            throw new DaoException(DUPLICATE_ERROR, e);
        }
    }

    @Override
    @Transactional(readOnly = false)
    public void savePerformers(final long dftId, final List<Integer> performerIds) {
        getJdbcTemplate().batchUpdate(
                "insert into department_form_type_performer (DEPARTMENT_FORM_TYPE_ID, PERFORMER_DEP_ID) " +
                        " values (?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setLong(1, dftId);
                        ps.setInt(2, performerIds.get(i));
                    }

                    @Override
                    public int getBatchSize() {
                        return performerIds.size();
                    }
                });
    }

    @Override
    public void deletePerformers(int dftId) {
        getJdbcTemplate().update(
                "delete from department_form_type_performer where DEPARTMENT_FORM_TYPE_ID = ?", dftId);
    }


    private static final String CHECK_EXIST = "select id from department_form_type src_dft where "
            + "department_id = ? and form_type_id= ? and kind = ? ";

    @Override
    public boolean existAssignedForm(int departmentId, int typeId, FormDataKind kind){
        return !getJdbcTemplate().queryForList(
                    CHECK_EXIST,
                    new Object[]{
                        departmentId,
                        typeId,
                        kind.getId()
                    },
                    Integer.class
                ).isEmpty();
    }

    private static final String EXIST_ACCEPTED_DESTINATIONS = "select dtype.name as declarationType, d.name as departmentName from declaration_data dd\n" +
            "join department_report_period drp on drp.id = dd.department_report_period_id\n" +
            "join declaration_template dt on dt.id = dd.declaration_template_id\n" +
            "join declaration_type dtype on dtype.id = dt.declaration_type_id \n" +
            "join department d on d.id = drp.department_id\n" +
            "join department_declaration_type ddt on (ddt.department_id = d.id and ddt.declaration_type_id = dtype.id)\n" +
            "join declaration_source ds on ds.department_declaration_type_id = ddt.id\n" +
            "join department_form_type dft on dft.id = ds.src_department_form_type_id\n" +
            "where dft.department_id = :sourceDepartmentId and dft.kind = :sourceKind and dft.form_type_id = :sourceFormTypeId and drp.id = :departmentReportPeriodId and dd.is_accepted = 1\n" +
            "and (:periodStart is null or ((ds.period_end >= :periodStart or ds.period_end is null) and (:periodEnd is null or ds.period_start <= :periodEnd)))";

    @Override
    public List<Pair<String, String>> existAcceptedDestinations(int sourceDepartmentId, int sourceFormTypeId,
                                                                FormDataKind sourceKind, Integer departmentReportPeriodId,
                                                                Date periodStart, Date periodEnd) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("sourceDepartmentId", sourceDepartmentId);
        params.put("sourceKind", sourceKind.getId());
        params.put("sourceFormTypeId", sourceFormTypeId);
        params.put("departmentReportPeriodId", departmentReportPeriodId);
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

    private static final String FIND_DESTINATIONS_SQL =
            "SELECT\n" +
                    "  tgt.id            id,\n" +
                    "  tgt.DEPARTMENT_ID department_id,\n" +
                    "  tgt.FORM_TYPE_ID  form_type_id,\n" +
                    "  tgt.KIND          kind,\n" +
                    "  fds.PERIOD_START  start_date,\n" +
                    "  fds.PERIOD_END    end_date,\n" +
                    "  ft.tax_type       tax_type\n" +
                    "FROM department_form_type src\n" +
                    "  JOIN form_data_source fds ON src.id = fds.src_department_form_type_id\n" +
                    "  JOIN department_form_type tgt ON fds.department_form_type_id = tgt.id\n" +
                    "  join form_type ft on ft.id = tgt.form_type_id \n" +
                    "WHERE src.form_type_id = :formTypeId AND\n" +
                    "      ((:dateTo IS NULL AND (fds.PERIOD_START >= :dateFrom OR fds.PERIOD_END >= :dateFrom))\n" +
                    "       OR (fds.PERIOD_START BETWEEN :dateFrom AND :dateTo OR fds.PERIOD_END BETWEEN :dateFrom AND :dateTo))";
    @Override
    public List<Pair<DepartmentFormType, Pair<Date, Date>>> findDestinationsForFormType(int typeId, Date dateFrom, Date dateTo) {
        try {
            Map<String, Object> values = new HashMap<String, Object>();
            values.put("formTypeId", typeId);
            values.put("dateFrom", dateFrom);
            values.put("dateTo", dateTo);

            return getNamedParameterJdbcTemplate().query(
                    FIND_DESTINATIONS_SQL,
                    values,
                    DFT_SOURCES_MAPPER);
        } catch (DataAccessException e){
            LOG.error("", e);
            throw new DaoException("", e);
        }
    }

    private static final String FIND_SOURCES_SQL =
            "SELECT\n" +
            "  src.id            id,\n" +
            "  src.DEPARTMENT_ID department_id,\n" +
            "  src.FORM_TYPE_ID  form_type_id,\n" +
            "  src.KIND          kind,\n" +
            "  fds.PERIOD_START  start_date,\n" +
            "  fds.PERIOD_END    end_date,\n" +
            "  ft.tax_type       tax_type\n" +
            "FROM department_form_type src\n" +
            "  JOIN form_data_source fds ON src.id = fds.src_department_form_type_id\n" +
            "  JOIN department_form_type tgt ON fds.department_form_type_id = tgt.id\n" +
            "  join form_type ft on ft.id = src.form_type_id \n" +
            "WHERE tgt.form_type_id = :formTypeId AND ((:dateTo IS NULL and (fds.PERIOD_START >= :dateFrom OR fds.PERIOD_END >= :dateFrom))\n" +
            "       OR (fds.PERIOD_START BETWEEN :dateFrom AND :dateTo or fds.PERIOD_END BETWEEN :dateFrom AND :dateTo))";
    @Override
    public List<Pair<DepartmentFormType, Pair<Date, Date>>> findSourcesForFormType(int typeId, Date dateFrom, Date dateTo) {
        try {
            Map<String, Object> values = new HashMap<String, Object>();
            values.put("formTypeId", typeId);
            values.put("dateFrom", dateFrom);
            values.put("dateTo", dateTo);

            return getNamedParameterJdbcTemplate().query(
                    FIND_SOURCES_SQL,
                    values,
                    DFT_SOURCES_MAPPER);
        } catch (DataAccessException e){
            LOG.error("", e);
            throw new DaoException("", e);
        }
    }

    @Override
    public List<DepartmentFormType> getDFTByFormType(Integer formTypeId) {
        try {
            List<DepartmentFormType> result = new ArrayList<DepartmentFormType>();
            getJdbcTemplate().query("select dft.id, dft.department_id, dft.form_type_id, dft.kind, dftp.performer_dep_id, ft.tax_type from DEPARTMENT_FORM_TYPE dft " +
                            "left join department_form_type_performer dftp on dftp.DEPARTMENT_FORM_TYPE_ID = dft.id \n" +
                            "join form_type ft on ft.id = dft.form_type_id \n" +
                    "where dft.FORM_TYPE_ID = ?",
                    new Object[]{formTypeId},
                    new DFTCallBackHandler(result, false));
            return result;
        }catch (DataAccessException e){
            LOG.error("Получение назначений НФ", e);
            throw new DaoException("Получение назначений НФ", e);
        }
    }

    @Override
    public int getAssignedFormsCount(List<Long> departmentsIds, char taxType) {

        QueryData assignedFormsQueryData = getAssignedFormsQueryData(departmentsIds, taxType, null, true);

        String query = "SELECT count(*) FROM ( " + assignedFormsQueryData.getQuery() + " )";

        return getNamedParameterJdbcTemplate().queryForObject(query, assignedFormsQueryData.getParameterSource(), Integer.class);
    }

    private String getSortingClause(QueryParams queryParams) {
        SourcesSearchOrdering ordering = (SourcesSearchOrdering) queryParams.getSearchOrdering();
        if (ordering == null) ordering = SourcesSearchOrdering.TYPE;

        boolean isAscSorting = queryParams.isAscending();
        StringBuilder sorting = new StringBuilder();

        List<String> columns = new ArrayList<String>();

        switch (ordering) {
            case TYPE:
                columns.add("ft.name\n");
                break;
            case KIND:
                columns.add("src_dft.kind\n");
                columns.add("ft.name\n");
                break;
            case TAX_TYPE:
                columns.add("tax_type\n");
                break;
            case DEPARTMENT:
                columns.add("d.name\n");
                break;
            case START:
                columns.add("ds.period_start\n");
                break;
            case END:
                columns.add("ds.period_end\n");
                break;
        }

        if (!columns.isEmpty()) {
            sorting.append(" ORDER BY ");
            Iterator iterator = columns.iterator();
            while (iterator.hasNext()) {
                sorting.append(iterator.next());
                if (!isAscSorting) {
                    sorting.append("DESC\n");
                }
                if (iterator.hasNext()) {
                    sorting.append(", ");
                }
            }
        }

        return sorting.toString();
    }

    /**
     * Фильтр по умолчанию
     *
     */
    private QueryParams<SourcesSearchOrdering> getSearchOrderingDefaultFilter() {
        QueryParams<SourcesSearchOrdering> filter = new QueryParams<SourcesSearchOrdering>();
        filter.setSearchOrdering(SourcesSearchOrdering.TYPE);
        filter.setAscending(true);
        return filter;
    }
}
