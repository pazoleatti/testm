package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DepartmentDeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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

	public static final RowMapper<DepartmentDeclarationType> DEPARTMENT_DECLARATION_TYPE_ROW_MAPPER =
			new RowMapper<DepartmentDeclarationType>() {

		@Override
		public DepartmentDeclarationType mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			DepartmentDeclarationType departmentDeclarationType = new DepartmentDeclarationType();
			departmentDeclarationType.setId(SqlUtils.getInteger(rs, "id"));
			departmentDeclarationType.setDeclarationTypeId(SqlUtils.getInteger(rs,"declaration_type_id"));
			departmentDeclarationType.setDepartmentId(SqlUtils.getInteger(rs,"department_id"));
			return departmentDeclarationType;
		}
	};

    private static final RowMapper<Pair<DepartmentFormType, Date>> DFT_SOURCES_MAPPER = new RowMapper<Pair<DepartmentFormType, Date>>() {

        @Override
        public Pair<DepartmentFormType, Date> mapRow(ResultSet rs, int i) throws SQLException {
            DepartmentFormType departmentFormType = new DepartmentFormType();
            departmentFormType.setFormTypeId(SqlUtils.getInteger(rs,"form_type_id"));
            departmentFormType.setDepartmentId(SqlUtils.getInteger(rs,"department_id"));
            departmentFormType.setKind(FormDataKind.fromId(SqlUtils.getInteger(rs,"kind")));
            return new Pair<DepartmentFormType, Date>(departmentFormType, rs.getDate("start_date"));
        }
    };

    private static final RowMapper<Pair<DepartmentDeclarationType, Date>> DDT_SOURCES_MAPPER = new RowMapper<Pair<DepartmentDeclarationType, Date>>() {

        @Override
        public Pair<DepartmentDeclarationType, Date> mapRow(ResultSet rs, int i) throws SQLException {
            DepartmentDeclarationType departmentDeclarationType = new DepartmentDeclarationType();
            departmentDeclarationType.setId(rs.getInt("id"));
            departmentDeclarationType.setDeclarationTypeId(SqlUtils.getInteger(rs,"declaration_type_id"));
            departmentDeclarationType.setDepartmentId(SqlUtils.getInteger(rs,"department_id"));
            return new Pair<DepartmentDeclarationType, Date>(departmentDeclarationType, rs.getDate("start_date"));
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
	public List<DepartmentDeclarationType> getByTaxType(int departmentId, TaxType taxType, Date periodStart,
                                                        Date periodEnd, Boolean isAscSorting) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("departmentId", departmentId);
        params.put("periodEnd", periodEnd);
        params.put("periodStart", periodStart);
        params.put("taxType", taxType != null ? String.valueOf(taxType.getCode()) : null);
        StringBuilder sql = new StringBuilder(GET_SQL_BY_TAX_TYPE_SQL);
        if (!isAscSorting)  sql.append("desc");
        return getNamedParameterJdbcTemplate().query(sql.toString(), params, DEPARTMENT_DECLARATION_TYPE_ROW_MAPPER);
	}

    @Override
    public List<DepartmentDeclarationType> getByTaxType(int departmentId, TaxType taxType) {
        return getJdbcTemplate().query(
                "select * from department_declaration_type ddt where department_id = ?" +
                        " and exists (select 1 from declaration_type dt where dt.id = ddt.declaration_type_id " +
                        (taxType != null ? "and dt.tax_type in " +SqlUtils.transformTaxTypeToSqlInStatement(Arrays.asList(taxType)) : "") + ")",
                new Object[] {
                        departmentId
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
                    departmentId, declarationTypeId);
		} catch (DataIntegrityViolationException e) {
    		throw new DaoException("Декларация указанного типа уже назначена подразделению", e);
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

    @Override
    public List<Pair<DepartmentDeclarationType, Date>> findDestinationDTsForFormType(int typeId, Date dateFrom, Date dateTo) {
        try {
            HashMap<String, Object> values = new HashMap<String, Object>();
            values.put("formTypeId", typeId);
            values.put("dateFrom", dateFrom);
            values.put("dateTo", dateTo);

            return getNamedParameterJdbcTemplate().query("select tgt.id id, tgt.DEPARTMENT_ID department_id, tgt.DECLARATION_TYPE_ID declaration_type_id, ds.PERIOD_START start_date\n" +
                    "from department_form_type src \n" +
                    "join declaration_source ds on src.id = ds.src_department_form_type_id\n" +
                    "join department_declaration_type tgt on ds.department_declaration_type_id = tgt.id \n" +
                    "  where \n" +
                    "    src.form_type_id=:formTypeId \n" +
                    "   AND (ds.PERIOD_START BETWEEN :dateFrom AND :dateTo " +
                    "   OR ds.PERIOD_END BETWEEN :dateFrom AND :dateTo \n" +
                    "   OR (ds.PERIOD_START BETWEEN :dateFrom AND :dateTo \n" +
                    "   AND ds.PERIOD_END IS null))",
                    values,
                    DDT_SOURCES_MAPPER);
        } catch (DataAccessException e){
            logger.error("", e);
            throw new DaoException("", e);
        }
    }

    @Override
    public List<Pair<DepartmentFormType, Date>> findSourceFTsForDeclaration(int typeId, Date dateFrom, Date dateTo) {
        try {
            HashMap<String, Object> values = new HashMap<String, Object>();
            values.put("formTypeId", typeId);
            values.put("dateFrom", dateFrom);
            values.put("dateTo", dateTo);

            return getNamedParameterJdbcTemplate().query("select src.DEPARTMENT_ID department_id, src.FORM_TYPE_ID form_type_id, src.KIND kind, ds.PERIOD_START start_date\n" +
                    "from department_form_type src \n" +
                    "join declaration_source ds on src.id = ds.src_department_form_type_id\n" +
                    "join department_declaration_type tgt on ds.department_declaration_type_id = tgt.id \n" +
                    "  where \n" +
                    "    tgt.declaration_type_id = :formTypeId \n" +
                    "   AND (ds.PERIOD_START BETWEEN :dateFrom AND :dateTo " +
                    "   OR ds.PERIOD_END BETWEEN :dateFrom AND :dateTo \n" +
                    "   OR (ds.PERIOD_START BETWEEN :dateFrom AND :dateTo \n" +
                    "   AND ds.PERIOD_END IS null))",
                    values,
                    DFT_SOURCES_MAPPER);
        } catch (DataAccessException e){
            logger.error("", e);
            throw new DaoException("", e);
        }
    }

    @Override
    public List<DepartmentDeclarationType> getDDTByDeclarationType(Integer declarationTypeId) {
        try {
            return getJdbcTemplate().query("select id, department_id, DECLARATION_TYPE_ID from DEPARTMENT_DECLARATION_TYPE where DECLARATION_TYPE_ID = ?",
                    new Object[]{declarationTypeId},
                    DEPARTMENT_DECLARATION_TYPE_ROW_MAPPER);
        } catch (DataAccessException e){
            logger.error("Получение списка деклараций назначений", e);
            throw new DaoException("Получение списка деклараций назначений", e);
        }
    }

    private final RowMapper<FormTypeKind> ALL_DECLARATION_ASSIGN_MAPPER = new RowMapper<FormTypeKind>() {
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
            department.setTbIndex(rs.getString("department_tb_index"));
            department.setSbrfCode(rs.getString("department_sbrf_code"));
            department.setRegionId(SqlUtils.getLong(rs, "department_region_id"));
            if (rs.wasNull()) {
                department.setRegionId(null);
            }
            department.setActive(rs.getBoolean("department_is_active"));
            department.setCode(rs.getInt("department_code"));

            FormTypeKind formTypeKind = new FormTypeKind();
            formTypeKind.setId(SqlUtils.getLong(rs, "id"));
            formTypeKind.setName(rs.getString("name"));
            formTypeKind.setFormTypeId(SqlUtils.getLong(rs, "type_id"));
            formTypeKind.setDepartment(department);
            return formTypeKind;
        }
    };

    @Override
    public List<FormTypeKind> getAllDeclarationAssigned(List<Long> departmentIds, char taxType, TaxNominationFilter filter) {

        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("params", departmentIds)
                .addValue("taxType", String.valueOf(taxType));

        String sql = "SELECT ddt.ID,\n" +
                "  dt.NAME,\n" +
                "  dt.ID AS type_id,\n" +
                "  ddt.department_id,\n" +
                "  -- Для подразделения\n" +
                "  d.ID        AS department_id,\n" +
                "  d.NAME      AS department_name,\n" +
                "  d.PARENT_ID AS department_parent_id,\n" +
                "  d.TYPE      AS department_type,\n" +
                "  d.SHORTNAME AS department_short_name,\n" +
                "  d.TB_INDEX  AS department_tb_index,\n" +
                "  d.SBRF_CODE AS department_sbrf_code,\n" +
                "  d.REGION_ID AS department_region_id,\n" +
                "  d.IS_ACTIVE AS department_is_active,\n" +
                "  d.CODE      AS department_code,\n" +
                "  -- Для сортировки\n" +
                "  dt.NAME AS dec_type,\n" +
                "  d.NAME  AS department\n" +
                "FROM declaration_type dt\n" +
                "JOIN department_declaration_type ddt\n" +
                "ON ddt.DECLARATION_TYPE_ID = dt.ID\n" +
                "JOIN department d\n" +
                "ON d.id = ddt.DEPARTMENT_ID\n" +
                "WHERE ddt.DEPARTMENT_ID IN (:params)\n" +
                "AND dt.TAX_TYPE = :taxType\n";

        String order = null;

        if (filter.getSortColumn() != null) {
            order = "ORDER BY " + filter.getSortColumn().name();
            if (!filter.isAscSorting())
                order = order + " DESC";
        }

        return getNamedParameterJdbcTemplate().query(sql + order, parameters, ALL_DECLARATION_ASSIGN_MAPPER);
    }
}
