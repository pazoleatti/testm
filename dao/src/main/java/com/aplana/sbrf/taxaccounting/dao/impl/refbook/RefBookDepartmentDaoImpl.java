package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.FormatUtils;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDepartmentDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentType;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.filter.DepartmentFilter;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDepartment;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Реализация дао для работы со справочником Подразделения
 */
@Repository
public class RefBookDepartmentDaoImpl extends AbstractDao implements RefBookDepartmentDao {

    @Override
    public RefBookDepartment fetchDepartmentById(Integer id) {
        String query = "SELECT * FROM ( " +
                REF_BOOK_DEPARTMENT_SELECT +
                "WHERE dep.is_active = 1 AND dep.id = ? " +
                ") WHERE rownum = 1";
        return getJdbcTemplate().queryForObject(query, new Object[]{id}, new RefBookDepartmentRowMapper());
    }

    @Override
    public RefBookDepartment findParentTBById(int id) {
        return getJdbcTemplate().queryForObject("" +
                        REF_BOOK_DEPARTMENT_SELECT +
                        "WHERE dep.parent_id = 0 AND dep.type = 2 " +
                        "START WITH dep.id = ? CONNECT BY dep.id = PRIOR dep.parent_id ",
                new RefBookDepartmentRowMapper(), id);
    }

    @Override
    public List<RefBookDepartment> findAllByName(String name, boolean exactSearch) {
        String nameLike = exactSearch ? name : "%" + name.toLowerCase() + "%";
        Object[] params = null;
        String whereClause = "";
        if (isNotEmpty(name)) {
            whereClause = "WHERE " + (exactSearch ? "name" : "lower(name)") + " LIKE ?";
            params = new Object[]{nameLike};
        }
        List<RefBookDepartment> departments = getJdbcTemplate().query(
                "WITH deps AS (" +
                        "   SELECT id FROM department " + whereClause +
                        ")" +
                        REF_BOOK_DEPARTMENT_SELECT +
                        "WHERE dep.id IN (" +
                        "   SELECT id FROM deps" +
                        "   UNION ALL" +
                        "   SELECT parent_id FROM DEPARTMENT_CHILD_VIEW WHERE id IN (SELECT id FROM deps)" +
                        ")", params, new RefBookDepartmentRowMapper());

        return departments;
    }

    @Override
    public List<RefBookDepartment> findAllByNameAsTree(String name, boolean exactSearch) {
        return asTree(findAllByName(name, exactSearch));
    }

    private List<RefBookDepartment> asTree(List<RefBookDepartment> departments) {
        List<RefBookDepartment> rootDepartments = new ArrayList<>();
        Map<Integer, RefBookDepartment> dtoByIdCache = new HashMap<>();
        for (RefBookDepartment department : departments) {
            dtoByIdCache.put(department.getId(), department);
        }
        for (RefBookDepartment department : departments) {
            if (department.getParentId() != null) {// Сбербанк пропускаем
                RefBookDepartment parent = dtoByIdCache.get(department.getParentId());
                department.setParent(parent);
                if (department.getType() == DepartmentType.TERR_BANK) {
                    rootDepartments.add(department);
                } else {
                    parent.addChild(department);
                }
            }
        }
        return rootDepartments;
    }

    @Override
    public List<RefBookDepartment> findAllActiveByIds(Collection<Integer> ids) {
        return findAllByIds(ids);
    }

    private List<RefBookDepartment> findAllByIds(Collection<Integer> ids) {
        String sql = REF_BOOK_DEPARTMENT_SELECT + "where dep.is_active = 1 AND dep.id IN (:ids)";
        return getNamedParameterJdbcTemplate().query(sql, new MapSqlParameterSource("ids", ids), new RefBookDepartmentRowMapper());
    }

    @Override
    public PagingResult<RefBookDepartment> fetchDepartments(Collection<Integer> ids, String name, PagingParams pagingParams) {
        return fetchDepartments(ids, name, false, pagingParams);
    }

    @Override
    public PagingResult<RefBookDepartment> findAllByFilter(DepartmentFilter filter, PagingParams pagingParams) {
        MapSqlParameterSource params = new MapSqlParameterSource();

        String baseSql = REF_BOOK_DEPARTMENT_SELECT;
        if (filter.getReportPeriodId() != null) {
            // фильтруем по наличию открытого периода
            baseSql += "join department_report_period drp on drp.department_id = dep.id and drp.is_active = 1 and drp.report_period_id = :reportPeriodId ";
            params.addValue("reportPeriodId", filter.getReportPeriodId());
        }
        if (filter.getAssignedToDeclarationTypeId() != null) {
            // фильтруем по наличию назначения подразделения типу формы
            baseSql += "join department_declaration_type ddt on ddt.department_id = dep.id and ddt.declaration_type_id = :declarationTypeId ";
            params.addValue("declarationTypeId", filter.getAssignedToDeclarationTypeId());
        }
        baseSql += "where dep.is_active = 1 ";
        if (filter.getIds() != null && !filter.getIds().isEmpty()) {
            baseSql += " and " + SqlUtils.transformToSqlInStatement("dep.id", filter.getIds());
        }
        if (filter.isOnlyTB()) {
            baseSql += " and dep.type = 2 ";
        }
        if (isNotBlank(filter.getName())) {
            baseSql += " and (lower(dep.name) like :name or lower(df.shortname) like :name) ";
            params.addValue("name", "%" + filter.getName().toLowerCase() + "%");
        }
        if (isNotBlank(pagingParams.getProperty()) && isNotBlank(pagingParams.getDirection())) {
            baseSql += new Formatter().format(" order by %s %s ",
                    FormatUtils.convertToUnderlineStyle(pagingParams.getProperty()),
                    pagingParams.getDirection()).toString();
        }

        params.addValue("startIndex", pagingParams.getStartIndex());
        params.addValue("count", pagingParams.getCount());
        List<RefBookDepartment> departments = getNamedParameterJdbcTemplate().query(
                "select * from ( select a.*, rownum rn from (\n" + baseSql + ") a \n) where rn > :startIndex and rowNum <= :count",
                params,
                new RefBookDepartmentRowMapper());

        int totalCount = getNamedParameterJdbcTemplate().queryForObject("select count(*) from (\n" + baseSql + ")", params, Integer.class);
        return new PagingResult<>(departments, totalCount);
    }

    @Override
    public List<RefBookDepartment> fetchAllActiveByType(DepartmentType type) {
        return getJdbcTemplate().query(REF_BOOK_DEPARTMENT_SELECT + " WHERE dep.type = ? AND dep.is_active = 1", new RefBookDepartmentRowMapper(), type.getCode());
    }

    @Override
    public String fetchFullName(Integer departmentId) {
        return getJdbcTemplate().queryForObject("SELECT shortname AS full_name FROM department_fullpath WHERE id = ?",
                String.class, departmentId);
    }

    @Override
    public PagingResult<RefBookDepartment> findDepartments(String name, PagingParams pagingParams) {
        String baseSql = REF_BOOK_DEPARTMENT_SELECT;

        MapSqlParameterSource params = new MapSqlParameterSource();

        if (isNotBlank(name)) {
            baseSql += "where (lower(dep.name) like :name or lower(df.shortname) like :name) ";
            params.addValue("name", "%" + name.toLowerCase() + "%");
        }

        if (isNotBlank(pagingParams.getProperty()) && isNotBlank(pagingParams.getDirection())) {
            baseSql += new Formatter().format("order by %s %s ",
                    FormatUtils.convertToUnderlineStyle(pagingParams.getProperty()),
                    pagingParams.getDirection()).toString();
        }

        params.addValue("startIndex", pagingParams.getStartIndex());
        params.addValue("count", pagingParams.getCount());

        List<RefBookDepartment> departments = getNamedParameterJdbcTemplate().query(
                "select * from ( select a.*, rownum rn from (\n" + baseSql + ") a \n) where rn > :startIndex and rowNum <= :count",
                params,
                new RefBookDepartmentRowMapper());

        int totalCount = getNamedParameterJdbcTemplate().queryForObject("select count(*) from (\n" + baseSql + ")", params, Integer.class);
        return new PagingResult<>(departments, totalCount);
    }

    @Override
    public List<RefBookDepartment> findActiveByTypeExcludingPresented(DepartmentType type, List<Integer> presentedTbIdList) {
        String excludeClause = presentedTbIdList != null ? "and dep.id not in (:presentedTbList)" :"";
        String query = REF_BOOK_DEPARTMENT_SELECT + " WHERE dep.type = :type AND dep.is_active = 1 " + excludeClause + " order by full_name";

        MapSqlParameterSource params = new MapSqlParameterSource("type", type.getCode());
        params.addValue("presentedTbList", presentedTbIdList);
        return getNamedParameterJdbcTemplate().query(query, params, new RefBookDepartmentRowMapper());
    }

    /**
     * Получение значений справочника по идентификаторам с фильтрацией по наименованию подразделения и пейджингом
     * Также можно выбрать все подразделения или только действующие
     *
     * @param ids          Список идентификаторов
     * @param name         Параметр фильтрации по наименованию подразделения, может содержаться в любой части полного
     *                     наименования или в любой части полного пути до подразделения, состоящего из кратких наименований
     * @param activeOnly   Искать только действующие подразделения
     * @param pagingParams Параметры пейджинга
     * @return Страница списка значений справочника
     */
    private PagingResult<RefBookDepartment> fetchDepartments(Collection<Integer> ids, String name, boolean activeOnly, PagingParams pagingParams) {
        String baseSql = String.format(REF_BOOK_DEPARTMENT_SELECT +
                "where %s ", SqlUtils.transformToSqlInStatement("dep.id", ids));

        MapSqlParameterSource params = new MapSqlParameterSource();

        if (isNotBlank(name)) {
            baseSql += " and (lower(dep.name) like :name or lower(df.shortname) like :name) ";
            params.addValue("name", "%" + name.toLowerCase() + "%");
        }
        if (activeOnly) {
            baseSql += " and dep.is_active = 1 ";
        }

        if (isNotBlank(pagingParams.getProperty()) && isNotBlank(pagingParams.getDirection())) {
            baseSql += new Formatter().format(" order by %s %s ",
                    FormatUtils.convertToUnderlineStyle(pagingParams.getProperty()),
                    pagingParams.getDirection()).toString();
        }

        params.addValue("startIndex", pagingParams.getStartIndex());
        params.addValue("count", pagingParams.getCount());

        List<RefBookDepartment> departments = getNamedParameterJdbcTemplate().query(
                "select * from ( select a.*, rownum rn from (\n" + baseSql + ") a \n) where rn > :startIndex and rowNum <= :count",
                params,
                new RefBookDepartmentRowMapper());

        int totalCount = getNamedParameterJdbcTemplate().queryForObject("select count(*) from (\n" + baseSql + ")", params, Integer.class);
        return new PagingResult<>(departments, totalCount);
    }

    private final static String REF_BOOK_DEPARTMENT_SELECT =
            "select dep.id, dep.name, dep.shortname, dep.parent_id, dep.type, dep.tb_index, dep.sbrf_code, " +
                    "dep.region_id, dep.is_active, dep.code, df.shortname as full_name " +
                    "from department dep " +
                    "inner join department_fullpath df on dep.id = df.id ";

    private final static class RefBookDepartmentRowMapper implements RowMapper<RefBookDepartment> {
        @Override
        public RefBookDepartment mapRow(ResultSet resultSet, int i) throws SQLException {
            RefBookDepartment refBookDepartment = new RefBookDepartment();

            refBookDepartment.setId(SqlUtils.getInteger(resultSet, "id"));
            refBookDepartment.setName(resultSet.getString("name"));
            refBookDepartment.setShortName(resultSet.getString("shortname"));
            refBookDepartment.setParentId(SqlUtils.getInteger(resultSet, "parent_id"));
            refBookDepartment.setType(DepartmentType.fromCode(resultSet.getInt("type")));
            refBookDepartment.setTbIndex(resultSet.getString("tb_index"));
            refBookDepartment.setSbrfCode(resultSet.getString("sbrf_code"));
            refBookDepartment.setRegionId(SqlUtils.getLong(resultSet, "region_id"));
            refBookDepartment.setActive(resultSet.getBoolean("is_active"));
            refBookDepartment.setCode(SqlUtils.getLong(resultSet, "code"));
            refBookDepartment.setFullName(resultSet.getString("full_name"));

            return refBookDepartment;
        }
    }
}
