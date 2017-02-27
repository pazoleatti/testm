package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DepartmentDeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import java.sql.Types;
import java.util.*;

/**
 * Реализация DAO для работы с информацией о назначении деклараций подразделениям
 * @author Eugene Stetsenko
 */
@Repository
@Transactional
public class DepartmentDeclarationTypeDaoImpl extends AbstractDao implements DepartmentDeclarationTypeDao {

	private static final Log LOG = LogFactory.getLog(DepartmentDeclarationTypeDaoImpl.class);

	public final RowMapper<DepartmentDeclarationType> DEPARTMENT_DECLARATION_TYPE_ROW_MAPPER =
			new RowMapper<DepartmentDeclarationType>() {

		@Override
		public DepartmentDeclarationType mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			DepartmentDeclarationType departmentDeclarationType = new DepartmentDeclarationType();
			departmentDeclarationType.setId(SqlUtils.getInteger(rs, "id"));
			departmentDeclarationType.setDeclarationTypeId(SqlUtils.getInteger(rs,"declaration_type_id"));
			departmentDeclarationType.setDepartmentId(SqlUtils.getInteger(rs,"department_id"));
            departmentDeclarationType.setTaxType(TaxType.fromCode(rs.getString("tax_type").charAt(0)));
            departmentDeclarationType.setPerformers(getPerformers(departmentDeclarationType.getId()));
			return departmentDeclarationType;
		}
	};

    private static final RowMapper<Pair<DepartmentFormType, Pair<Date, Date>>> DFT_SOURCES_MAPPER = new RowMapper<Pair<DepartmentFormType, Pair<Date, Date>>>() {

        @Override
        public Pair<DepartmentFormType, Pair<Date, Date>> mapRow(ResultSet rs, int i) throws SQLException {
            DepartmentFormType departmentFormType = new DepartmentFormType();
            departmentFormType.setFormTypeId(SqlUtils.getInteger(rs, "form_type_id"));
            departmentFormType.setDepartmentId(SqlUtils.getInteger(rs, "department_id"));
            departmentFormType.setKind(FormDataKind.fromId(SqlUtils.getInteger(rs, "kind")));
            departmentFormType.setTaxType(TaxType.fromCode(rs.getString("tax_type").charAt(0)));
            Pair<Date, Date> dates = new Pair<Date, Date>(rs.getDate("start_date"), rs.getDate("end_date"));
            return new Pair<DepartmentFormType, Pair<Date, Date>>(departmentFormType, dates);
        }
    };

    private final RowMapper<Pair<DepartmentDeclarationType, Pair<Date, Date>>> DDT_SOURCES_MAPPER = new RowMapper<Pair<DepartmentDeclarationType, Pair<Date, Date>>>() {

        @Override
        public Pair<DepartmentDeclarationType, Pair<Date, Date>> mapRow(ResultSet rs, int i) throws SQLException {
            DepartmentDeclarationType departmentDeclarationType = new DepartmentDeclarationType();
            departmentDeclarationType.setId(rs.getInt("id"));
            departmentDeclarationType.setDeclarationTypeId(SqlUtils.getInteger(rs,"declaration_type_id"));
            departmentDeclarationType.setDepartmentId(SqlUtils.getInteger(rs,"department_id"));
            departmentDeclarationType.setTaxType(TaxType.fromCode(rs.getString("tax_type").charAt(0)));
            departmentDeclarationType.setPerformers(getPerformers(departmentDeclarationType.getId()));
            Pair<Date, Date> dates = new Pair<Date, Date>(rs.getDate("start_date"), rs.getDate("end_date"));
            return new Pair<DepartmentDeclarationType, Pair<Date, Date>>(departmentDeclarationType, dates);
        }
    };

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

    private final static String GET_SQL_BY_TAX_TYPE_SQL = "select * from department_declaration_type ddt\n" +
            "left join declaration_type dt ON ddt.declaration_type_id = dt.id\n" +
            "where department_id = :departmentId and exists (\n" +
            "select 1 from declaration_type dt \n" +
            "left join declaration_template dtemp on dtemp.declaration_type_id = dt.id\n" +
            "where dt.id = ddt.declaration_type_id and (:taxType is null or dt.tax_type = :taxType) \n" +
            "and (:periodStart is null or (dtemp.version >= :periodStart or (:periodEnd is null or dtemp.version <= :periodEnd)))\n" +
            ") order by dt.name\n";

	@Override
	public List<DepartmentDeclarationType> getByTaxType(int departmentId, TaxType taxType, Date periodStart, Date periodEnd) {
        QueryParams queryParams = new QueryParams();
        queryParams.setAscending(true);
        return getByTaxType(departmentId, taxType, periodStart, periodEnd, queryParams);
    }

    @Override
    public List<DepartmentDeclarationType> getByTaxType(int departmentId, TaxType taxType, Date periodStart, Date periodEnd, QueryParams queryParams) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("departmentId", departmentId);
        params.put("periodEnd", periodEnd);
        params.put("periodStart", periodStart);
        params.put("taxType", taxType != null ? String.valueOf(taxType.getCode()) : null);
        StringBuilder sql = new StringBuilder(GET_SQL_BY_TAX_TYPE_SQL);
        if (!queryParams.isAscending())  sql.append("desc");
        return getNamedParameterJdbcTemplate().query(sql.toString(), params, DEPARTMENT_DECLARATION_TYPE_ROW_MAPPER);
    }

    @Override
    public List<DepartmentDeclarationType> getByTaxType(int departmentId, TaxType taxType) {
        return getJdbcTemplate().query(
                "select * from department_declaration_type ddt "+
                        "join declaration_type dt1 on dt1.id = ddt.declaration_type_id \n" +
                        "where department_id = ?" +
                        " and exists (select 1 from declaration_type dt where dt.id = ddt.declaration_type_id " +
                        (taxType != null ? "and dt.tax_type in " +SqlUtils.transformTaxTypeToSqlInStatement(Arrays.asList(taxType)) : "") + ")",
                new Object[] {
                        departmentId
                },
                DEPARTMENT_DECLARATION_TYPE_ROW_MAPPER
        );
    }

	@Override
	public long save(int departmentId, int declarationTypeId) {
		try {
            long id = generateId("SEQ_DEPT_DECLARATION_TYPE", Long.class);
            getJdbcTemplate().update(
	                "insert into department_declaration_type (id, department_id, declaration_type_id) " +
	                        " values (?, ?, ?)",
                    id, departmentId, declarationTypeId);
            return id;
		} catch (DataIntegrityViolationException e) {
    		throw new DaoException("Налоговая форма уже назначена подразделению", e);
    	}
	}

	@Override
	public void delete(Long id) {
		try{
	        getJdbcTemplate().update(
	                "delete from department_declaration_type where id = ?",
                    id);
		} catch (DataIntegrityViolationException e){
			throw new DaoException("Назначение является приемником данных для форм", e);
		}
	}


    private static final String FIND_DT_DESTINATIONS_FOR_FORM_TYPE =
            "SELECT\n" +
                    "  tgt.id                  id,\n" +
                    "  tgt.DEPARTMENT_ID       department_id,\n" +
                    "  tgt.DECLARATION_TYPE_ID declaration_type_id,\n" +
                    "  ds.PERIOD_START         start_date,\n" +
                    "  ds.PERIOD_END           end_date,\n" +
                    "  dt.tax_type             tax_type\n" +
                    "FROM department_form_type src\n" +
                    "  JOIN declaration_source ds ON src.id = ds.src_department_form_type_id\n" +
                    "  JOIN department_declaration_type tgt ON ds.department_declaration_type_id = tgt.id\n" +
                    "  join declaration_type dt on dt.id = tgt.declaration_type_id \n" +
                    "WHERE\n" +
                    "  src.form_type_id = :formTypeId\n" +
                    "  AND ((:dateTo IS NULL AND (ds.PERIOD_START >= :dateFrom OR ds.PERIOD_END >= :dateFrom))\n" +
                    "       OR (ds.PERIOD_START BETWEEN :dateFrom AND :dateTo OR\n" +
                    "           ds.PERIOD_END BETWEEN :dateFrom AND :dateTo))";
    @Override
    public List<Pair<DepartmentDeclarationType, Pair<Date, Date>>> findDestinationDTsForFormType(int typeId, Date dateFrom, Date dateTo) {
        try {
            HashMap<String, Object> values = new HashMap<String, Object>();
            values.put("formTypeId", typeId);
            values.put("dateFrom", dateFrom);
            values.put("dateTo", dateTo);

            return getNamedParameterJdbcTemplate().query(
                    FIND_DT_DESTINATIONS_FOR_FORM_TYPE,
                    values,
                    DDT_SOURCES_MAPPER);
        } catch (DataAccessException e){
			LOG.error("", e);
            throw new DaoException("", e);
        }
    }


    private static final String FIND_FT_SOURCES_FOR_DECLARATION =
            "SELECT\n" +
                    "  src.DEPARTMENT_ID department_id,\n" +
                    "  src.FORM_TYPE_ID  form_type_id,\n" +
                    "  src.KIND          kind,\n" +
                    "  ds.PERIOD_START   start_date,\n" +
                    "  ds.PERIOD_END     end_date\n," +
                    "  ft.tax_type     tax_type\n" +
                    "FROM department_form_type src\n" +
                    "  JOIN declaration_source ds ON src.id = ds.src_department_form_type_id\n" +
                    "  JOIN department_declaration_type tgt ON ds.department_declaration_type_id = tgt.id\n" +
                    "  join form_type ft on ft.id = src.form_type_id \n" +
                    "WHERE\n" +
                    "  tgt.declaration_type_id = :formTypeId\n" +
                    "  AND ((:dateTo IS NULL AND (ds.PERIOD_START >= :dateFrom OR ds.PERIOD_END >= :dateFrom))\n" +
                    "       OR (ds.PERIOD_START BETWEEN :dateFrom AND :dateTo OR\n" +
                    "           ds.PERIOD_END BETWEEN :dateFrom AND :dateTo))";
    @Override
    public List<Pair<DepartmentFormType, Pair<Date, Date>>> findSourceFTsForDeclaration(int typeId, Date dateFrom, Date dateTo) {
        try {
            HashMap<String, Object> values = new HashMap<String, Object>();
            values.put("formTypeId", typeId);
            values.put("dateFrom", dateFrom);
            values.put("dateTo", dateTo);

            return getNamedParameterJdbcTemplate().query(FIND_FT_SOURCES_FOR_DECLARATION,
                    values,
                    DFT_SOURCES_MAPPER);
        } catch (DataAccessException e){
			LOG.error("", e);
            throw new DaoException("", e);
        }
    }

    @Override
    public List<DepartmentDeclarationType> getDDTByDeclarationType(Integer declarationTypeId) {
        try {
            return getJdbcTemplate().query("select ddt.id, ddt.department_id, ddt.DECLARATION_TYPE_ID, dt.tax_type from DEPARTMENT_DECLARATION_TYPE ddt "+
                    "join declaration_type dt on dt.id = ddt.declaration_type_id \n" +
                    "where ddt.DECLARATION_TYPE_ID = ?",
                    new Object[]{declarationTypeId},
                    DEPARTMENT_DECLARATION_TYPE_ROW_MAPPER);
        } catch (DataAccessException e){
			LOG.error("Получение списка налоговых форм назначений", e);
            throw new DaoException("Получение списка налоговых форм назначений", e);
        }
    }

    private static final RowMapper<FormTypeKind> ALL_DECLARATION_ASSIGN_MAPPER = new RowMapper<FormTypeKind>() {
        @Override
        public FormTypeKind mapRow(ResultSet rs, int rowNum) throws SQLException {
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

            FormTypeKind formTypeKind = new FormTypeKind();
            formTypeKind.setId(SqlUtils.getLong(rs, "id"));
            formTypeKind.setName(rs.getString("name"));
            formTypeKind.setFormTypeId(SqlUtils.getLong(rs, "type_id"));
            formTypeKind.setDepartment(department);
            return formTypeKind;
        }
    };

    /**
     * Метод составляет запрос используя переданные ей параметры
     *
     * @param departmentIds подразделелния
     * @param taxType тип налога
     * @param queryParams параметры пейджинга, сортировки
     */

    private static final String QUERY =
            "SELECT "+
                    "ddt.id,ddt.name,ddt.type_id,ddt.department_id, \n" +
                    "ddt.department_name, \n" +
                    "ddt.department_parent_id, \n" +
                    "ddt.department_type, \n" +
                    "ddt.department_short_name, \n" +
                    "ddt.department_full_name, \n" +
                    "ddt.department_tb_index, \n" +
                    "ddt.department_sbrf_code, \n" +
                    "ddt.department_region_id, \n" +
                    "ddt.department_is_active, \n" +
                    "ddt.department_code, \n" +
                    "ddt.department_garant_use,\n" +
                    "ddt.department_sunr_use,\n" +
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
                    "SELECT ddtOrd.*,\n" +
                    "    rownum AS row_number_over\n" +
                    "  FROM(\n"+
                    "SELECT ddt.ID as id,\n" +
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
                    "  ft.NAME  AS dec_type,\n" +
                    "  d.NAME   AS department,\n" +
                    "  d.FULL_NAME AS department_full_name \n" +
                    "FROM department_declaration_type ddt\n" +
                    "JOIN declaration_type ft ON ft.ID = ddt.DECLARATION_TYPE_ID\n" +
                    "JOIN (SELECT d.*, LTRIM(SYS_CONNECT_BY_PATH(name, '/'), '/') as full_name FROM department d START with parent_id is null CONNECT BY PRIOR id = parent_id) d ON d.ID = ddt.DEPARTMENT_ID\n" +
                    "WHERE ft.tax_type = :taxType\n" +
                    "%s\n" +
                    ") ddtOrd \n" +
                    ") ddt \n" +
                    "left join department_decl_type_performer ddtp on 0=:withoutPerformers and ddtp.DEPARTMENT_DECL_TYPE_ID = ddt.id \n" +
                    "LEFT OUTER JOIN department dp ON dp.ID = ddtp.PERFORMER_DEP_ID\n" +
                    "LEFT OUTER JOIN (SELECT d.*, LTRIM(SYS_CONNECT_BY_PATH(name, '/'), '/') as full_name FROM department d START with parent_id is null CONNECT BY PRIOR id = parent_id) p ON p.ID = ddtp.PERFORMER_DEP_ID\n" +
                    "%s\n" +
                    "%s";

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
            set.add(TaxNominationColumnEnum.DEC_TYPE);

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
            departmentClause = "AND " + SqlUtils.transformToSqlInStatement("ddt.department_id", departmentIds) + "\n";
            parameters.addValue("params", departmentIds);
        }

        String whereClause = "";
        // Limit
        if (paging){
            whereClause = " WHERE ddt.row_number_over BETWEEN :from and :to";
            parameters.addValue("from", queryParams.getFrom()+1);
            parameters.addValue("to", queryParams.getFrom() + queryParams.getCount());
        }
        parameters.addValue("withoutPerformers", withoutPerformers ? 1 : 0);

        String query = String.format(QUERY, departmentClause, whereClause, order.toString());

        QueryData queryData = new QueryData();
        queryData.setQuery(query);
        queryData.setParameterSource(parameters);

        return queryData;
    }

    /**
     * Метод составляет запрос используя переданные ей параметры
     *
     * @param departmentIds подразделелния
     * @param taxType тип налога
     * @param queryParams параметры пейджинга, сортировки
     * @return
     */
    private QueryData getAssignedDeclarationsQueryData(List<Long> departmentIds, char taxType, QueryParams<TaxNominationColumnEnum> queryParams){
        boolean paging = queryParams != null && queryParams.getCount() != 0;

        MapSqlParameterSource parameters = new MapSqlParameterSource().addValue("taxType", String.valueOf(taxType));

        // departments
        String departmentClause = "";
        if (departmentIds != null && !departmentIds.isEmpty()){
            departmentClause = "AND " + SqlUtils.transformToSqlInStatement("ddt.DEPARTMENT_ID", departmentIds) + "\n";
        }

        // order
        StringBuffer order = new StringBuffer("");
        if (queryParams != null && queryParams.getSearchOrdering() != null) {
            String asc = queryParams.isAscending()?"":" DESC";
            Set<Enum<TaxNominationColumnEnum>> set = new LinkedHashSet<Enum<TaxNominationColumnEnum>>();
            set.add(queryParams.getSearchOrdering());
            set.add(TaxNominationColumnEnum.DEPARTMENT_FULL_NAME);
            set.add(TaxNominationColumnEnum.DEC_TYPE);

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

        String query =
                "SELECT \n"+
                    "id," +
                    "name," +
                    "type_id," +
                    "department_id, \n" +
                    "department_name, \n" +
                    "department_parent_id, \n" +
                    "department_type, \n" +
                    "department_short_name, \n" +
                    "department_full_name, \n" +
                    "department_tb_index, \n" +
                    "department_sbrf_code, \n" +
                    "department_region_id, \n" +
                    "department_is_active, \n" +
                    "department_code, \n" +
                    "department_garant_use, \n" +
                    "department_sunr_use \n" +
                    // пейджинг
                    (paging ? ", rownum as row_number_over \n":"") +
                "FROM ("+
                    "SELECT ddt.ID,\n" +
                    "  dt.NAME,\n" +
                    "  dt.ID AS type_id,\n" +
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
                    "  dt.NAME AS dec_type,\n" +
                    "  d.NAME  AS department,\n" +
                    "  d.FULL_NAME AS department_full_name \n" +
                    "FROM declaration_type dt\n" +
                    "JOIN department_declaration_type ddt\n" +
                    "ON ddt.DECLARATION_TYPE_ID = dt.ID\n" +
                    "JOIN (SELECT d.*, LTRIM(SYS_CONNECT_BY_PATH(name, '/'), '/') as full_name FROM department d START with parent_id = 0 CONNECT BY PRIOR id = parent_id) d \n" +
                    "ON d.id = ddt.DEPARTMENT_ID\n" +
                    "WHERE dt.TAX_TYPE = :taxType\n"+
                    departmentClause +
                    order.toString() +
                ")";

        // Limit
        if (queryParams != null && queryParams.getCount() != 0){
            query = "SELECT * FROM ( " + query + " ) WHERE row_number_over BETWEEN :from and :to";
            parameters.addValue("from", queryParams.getFrom()+1);
            parameters.addValue("to", queryParams.getFrom() + queryParams.getCount());
        }

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
    public List<FormTypeKind> getAllDeclarationAssigned(List<Long> departmentIds, char taxType, QueryParams<TaxNominationColumnEnum> queryParams) {
        QueryData assignedFormsQueryData = getAssignedFormsQueryData(departmentIds, taxType, queryParams, false);
        List<FormTypeKind> result = new ArrayList<FormTypeKind>();
        getNamedParameterJdbcTemplate().query(assignedFormsQueryData.getQuery(), assignedFormsQueryData.getParameterSource(), new AllAssignCallBackHandler(result));
        return result;
    }

    @Override
    public int getAssignedDeclarationsCount(List<Long> departmentsIds, char taxType) {
        QueryData assignedFormsQueryData = getAssignedFormsQueryData(departmentsIds, taxType, null, true);

        String query = "SELECT count(*) FROM ( " + assignedFormsQueryData.getQuery() + " )";

        return getNamedParameterJdbcTemplate().queryForObject(query, assignedFormsQueryData.getParameterSource(), Integer.class);
    }

    private List<Integer> getPerformers(int departmentDeclarationTypeId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("departmentDeclarationTypeId", departmentDeclarationTypeId);
        try {
            return getNamedParameterJdbcTemplate().queryForList("select performer_dep_id " +
                    "from department_decl_type_performer " +
                    "where department_decl_type_id = :departmentDeclarationTypeId", params, Integer.class);
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Integer>();
        }
    }


    @Override
    public void savePerformers(final long ddtId, final List<Integer> performerIds) {
        getJdbcTemplate().batchUpdate(
                "insert into department_decl_type_performer (DEPARTMENT_DECL_TYPE_ID, PERFORMER_DEP_ID) " +
                        " values (?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setLong(1, ddtId);
                        ps.setInt(2, performerIds.get(i));
                    }

                    @Override
                    public int getBatchSize() {
                        return performerIds.size();
                    }
                });
    }

    @Override
    public void deletePerformers(int ddtId) {
        getJdbcTemplate().update(
                "delete from department_decl_type_performer where DEPARTMENT_DECL_TYPE_ID = ?", ddtId);
    }
}
