package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.FormatUtils;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.CacheConstants;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentName;
import com.aplana.sbrf.taxaccounting.model.DepartmentShortInfo;
import com.aplana.sbrf.taxaccounting.model.DepartmentType;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.SecuredEntity;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.intellij.lang.annotations.Language;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Repository
@Transactional(readOnly = true)
public class DepartmentDaoImpl extends AbstractDao implements DepartmentDao {

    private static final Log LOG = LogFactory.getLog(DepartmentDaoImpl.class);

    @Override
    @Cacheable(CacheConstants.DEPARTMENT)
    public Department getDepartment(int id) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Fetching department with id = " + id + " from database");
        }
        try {
            return getJdbcTemplate().queryForObject(
                    "SELECT * FROM department WHERE id = ?",
                    new Object[]{id},
                    new DepartmentJdbcMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException("Не удалось найти подразделение банка с id = " + id);
        }
    }

    @Override
    public boolean existDepartment(int id) {
        return getJdbcTemplate().queryForObject("SELECT count(id) FROM department WHERE id = ?", new Object[]{id}, Integer.class) == 1;
    }

    @Override
    public List<Department> getChildren(int parentDepartmentId) {
        try {
            return getJdbcTemplate().query(
                    "SELECT * FROM department WHERE parent_id = ?",
                    new Object[]{parentDepartmentId},
                    new DepartmentJdbcMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>(0);
        }
    }

    @Override
    public String getParentsHierarchy(Integer departmentId) {
        try {
            return getJdbcTemplate().queryForObject(sqlParentHierarchy("name"),
                    new RowMapper<String>() {
                        @Override
                        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                            return rs.getString("path");
                        }
                    },
                    departmentId
            );
        } catch (EmptyResultDataAccessException e) {
            return getDepartment(departmentId).getName();
        }
    }

    @Override
    public String getParentsHierarchyShortNames(Integer departmentId) {
        try {
            return getJdbcTemplate().queryForObject(sqlParentHierarchy("shortname"),
                    new RowMapper<String>() {
                        @Override
                        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                            return rs.getString("path");
                        }
                    },
                    departmentId
            );
        } catch (EmptyResultDataAccessException e) {
            return getDepartment(departmentId).getShortName();
        }
    }

    private String sqlParentHierarchy(String columnName) {
        //language=sql
        return "SELECT LTRIM(SYS_CONNECT_BY_PATH(CASE WHEN " + columnName + " is not null THEN " + columnName + " ELSE name END, '/'), '/') as path \n" +
                "FROM department\n" +
                "WHERE id = ? \n" +
                "START WITH parent_id in (select id from department where parent_id is null)  \n" +
                "CONNECT BY PRIOR id = parent_id";
    }

    @Override
    public Integer getParentTBId(int departmentId) {
        try {
            return getJdbcTemplate().queryForObject("SELECT id FROM department WHERE parent_id = 0 AND type = 2 " +
                    "START WITH id = ? CONNECT BY id = PRIOR parent_id", new Object[]{departmentId}, Integer.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (DataAccessException e) {
            throw new DaoException("", e);
        }
    }

    @Override
    @Cacheable(value = CacheConstants.DEPARTMENT, key = "'parent_tb_'+#departmentId")
    public Department getParentTB(int departmentId) {
        try {
            return getJdbcTemplate().queryForObject("SELECT * FROM department WHERE parent_id = 0 AND type = 2 " +
                    "START WITH id = ? CONNECT BY id = PRIOR parent_id", new Object[]{departmentId}, new DepartmentJdbcMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (DataAccessException e) {
            throw new DaoException("", e);
        }
    }

    @Override
    public List<Department> listDepartments() {
        try {
            return getJdbcTemplate().query(
                    "SELECT * FROM department",
                    new DepartmentJdbcMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>(0);
        }
    }

    @Override
    public SecuredEntity getSecuredEntity(long id) {
        return getDepartment((int) id);
    }

    protected class DepartmentJdbcMapper implements RowMapper<Department> {
        @Override
        public Department mapRow(ResultSet rs, int rowNum) throws SQLException {
            try {
                Department department = new Department();
                department.setId(rs.getInt("id"));
                department.setName(rs.getString("name"));
                department.setParentId(SqlUtils.getInteger(rs, "parent_id"));
                department.setType(DepartmentType.fromCode(rs.getInt("type")));
                department.setShortName(rs.getString("shortname"));
                department.setTbIndex(rs.getString("tb_index"));
                department.setSbrfCode(rs.getString("sbrf_code"));
                department.setActive(rs.getBoolean("is_active"));
                department.setCode(rs.getLong("code"));
                department.setGarantUse(rs.getBoolean("garant_use"));
                department.setSunrUse(rs.getBoolean("sunr_use"));
                return department;
            } catch (EmptyResultDataAccessException e) {
                return null;
            }
        }
    }

    @Override
    public Department getDepartmentBySbrfCode(String sbrfCode, boolean activeOnly) {
        try {
            return getJdbcTemplate().queryForObject(
                    "SELECT * FROM department dp WHERE lower(dp.sbrf_code) = lower(?) AND (? = 0 OR dp.is_active = ?)",
                    new Object[]{sbrfCode, activeOnly ? 1 : 0, activeOnly ? 1 : 0},
                    new DepartmentJdbcMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<Department> getDepartmentsBySbrfCode(String sbrfCode, boolean activeOnly) {
        return getJdbcTemplate().query(
                "SELECT * FROM department dp WHERE lower(dp.sbrf_code) = lower(?) AND (? = 0 OR dp.is_active = ?)",
                new Object[]{sbrfCode, activeOnly ? 1 : 0, activeOnly ? 1 : 0},
                new DepartmentJdbcMapper()
        );
    }

    @Override
    public List<Department> getDepartmentsByType(int type) {
        try {
            return getJdbcTemplate().query(
                    "SELECT * FROM department dp WHERE dp.type = ?",
                    new Object[]{type},
                    new DepartmentJdbcMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>(0);
        }
    }

    @Override
    public List<Integer> getDepartmentIdsByType(int type) {
        try {
            return getJdbcTemplate().queryForList(
                    "SELECT id FROM department dp WHERE dp.type = ?",
                    new Object[]{type},
                    Integer.class
            );
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>(0);
        }
    }

    @Override
    public Department getDepartmentTB(int departmentId) {
        int typeIdTerbank = DepartmentType.TERR_BANK.getCode();
        try {
            String recursive = isWithRecursive() ? "recursive" : "";
            return getJdbcTemplate().queryForObject("with " + recursive + " tree (id, parent_id, type) as " +
                            "(select id, parent_id, type from department where id = ? " +
                            "union all select d.id, d.parent_id, d.type from department d " +
                            "inner join tree t on d.id = t.parent_id) " +
                            "select d.* from department d, tree where d.id = tree.id and tree.type = ?",
                    new Object[]{departmentId, typeIdTerbank},
                    new DepartmentJdbcMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<Department> getDepartmentTBChildren(int departmentId) {
        return getParentDepartmentChildByType(departmentId, 2);
    }

    @Override
    public List<Integer> getDepartmentTBChildrenId(int departmentId) {
        return getParentDepartmentChildIdByType(departmentId, 2);
    }

    @Override
    public List<Department> getRequiredForTreeDepartments(List<Integer> availableDepartments) {
        try {
            String recursive = isWithRecursive() ? "recursive" : "";
            return getJdbcTemplate().query(
                    "with " + recursive + " tree (id, parent_id) as " +
                            "(select id, parent_id from department where " +
                            SqlUtils.transformToSqlInStatement("id", availableDepartments) +
                            "union all " +
                            "select d.id, d.parent_id from department d inner join tree t on d.id = t.parent_id) " +
                            "select distinct d.* from tree t, department d where d.id = t.id",
                    new DepartmentJdbcMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>(0);
        }
    }

    /**
     * Получение родительского узла заданного типа (указанное подразделение м.б. результатом, если его тип соответствует искомому)
     * + все дочерние подразделения
     */
    private List<Department> getParentDepartmentChildByType(int departmentId, int typeId) {
        try {
            return getJdbcTemplate().query(createQueryParentDepartmentChildByType(false),
                    new Object[]{departmentId, typeId, typeId, typeId},
                    new DepartmentJdbcMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>(0);
        }
    }

    /**
     * Получение идентификатора родительского узла заданного типа (указанное подразделение м.б. результатом, если его тип соответствует искомому)
     * + все идентификаторы дочерних подразделений
     */
    private List<Integer> getParentDepartmentChildIdByType(int departmentId, int typeId) {
        try {
            return getJdbcTemplate().queryForList(createQueryParentDepartmentChildByType(true),
                    new Object[]{departmentId, typeId, typeId, typeId},
                    Integer.class
            );
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>(0);
        }
    }

    /**
     * Формирование запроса для методов
     *
     * @param idOnly true - выборка только идентификаторов, false - выборка полной модели
     * @return строку запроса
     * @see DepartmentDaoImpl#getParentDepartmentChildIdByType(int, int)
     * @see DepartmentDaoImpl#getParentDepartmentChildByType(int, int)
     */
    private String createQueryParentDepartmentChildByType(boolean idOnly) {
        String recursive = isWithRecursive() ? "recursive" : "";

        @Language("sql")
        String rez = "with " + recursive + " tree1 (id, parent_id, type) as " +
                "(select id, parent_id, type from department where id = ? " +
                "union all " +
                "select d.id, d.parent_id, d.type from " +
                "department d inner join tree1 t1 on d.id = t1.parent_id where d.type >= ?), " +
                "tree2 (id, root_id, type) as " +
                "(select id, id root_id, type from department where type = ? " +
                "union all select d.id, t2.root_id, d.type " +
                "from department d inner join tree2 t2 on d.parent_id = t2.id) " +
                (idOnly ? "select d.id " : "select d.* ") +                         // определяем
                "from tree1 t1, tree2 t2, department d where t1.type = ? " +
                "and t2.root_id = t1.id and t2.id = d.id";
        return rez;
    }

    @Override
    public List<Department> getAllChildren(int parentDepartmentId) {
        try {
            return getJdbcTemplate().query(
                    "SELECT * FROM department CONNECT BY PRIOR id = parent_id START WITH id = ?",
                    new Object[]{parentDepartmentId},
                    new DepartmentJdbcMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>(0);
        }
    }

    @Override
    public List<Integer> findAllIdsByPerformerIds(List<Integer> performersIds) {
        return getJdbcTemplate().queryForList("select distinct department_id from DEPARTMENT_DECLARATION_TYPE ddt\n" +
                        "inner join DEPARTMENT_DECL_TYPE_PERFORMER ddtp on ddt.id = ddtp.department_decl_type_id\n" +
                        "where " + SqlUtils.transformToSqlInStatement("performer_dep_id", performersIds),
                Integer.class);
    }

    @Override
    public List<Integer> findAllTBIdsByPerformerId(int performerDepartmentId) {
        Map<String, Object> params = new HashMap<>();
        params.put("performerDepartmentId", performerDepartmentId);
        String sql = "SELECT DISTINCT id\n" +
                "FROM department\n" +
                "WHERE parent_id = 0 AND type = 2\n" +
                "CONNECT BY id = PRIOR parent_id START WITH id IN (\n" +
                "  SELECT ddt.department_id\n" +
                "  FROM department_declaration_type ddt\n" +
                "  INNER JOIN department_decl_type_performer ddtp ON ddt.id = ddtp.department_decl_type_id\n" +
                "  WHERE ddtp.performer_dep_id = :performerDepartmentId\n" +
                ")";
        return getNamedParameterJdbcTemplate().queryForList(sql, params, Integer.class);
    }

    @Override
    public String getDepartmentNameByPairKppOktmo(String kpp, String oktmo, Date reportPeriodEndDate) {
        String query = "SELECT dep.name, max(rnd.version) " +
                "FROM department dep " +
                "JOIN ref_book_ndfl_detail rnd ON dep.id = rnd.department_id " +
                "JOIN ref_book_oktmo ro ON ro.id = rnd.oktmo " +
                "WHERE rnd.kpp = :kpp AND ro.code = :oktmo AND rnd.version <= :reportPeriodEndDate GROUP BY dep.NAME";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("kpp", kpp)
                .addValue("oktmo", oktmo)
                .addValue("reportPeriodEndDate", reportPeriodEndDate);
        List<String> result = getNamedParameterJdbcTemplate().query(query, params, new RowMapper<String>() {
            @Override
            public String mapRow(ResultSet resultSet, int i) throws SQLException {
                return resultSet.getString("name");
            }
        });
        if (result != null && !result.isEmpty()) {
            return result.get(0);
        }
        return "";
    }

    @Override
    public List<Integer> fetchAllIds() {
        return getJdbcTemplate().queryForList("SELECT id FROM department", Integer.class);
    }

    @Override
    public List<Integer> findAllChildrenIdsById(int parentDepartmentId) {
        return getJdbcTemplate().queryForList(
                "SELECT id FROM department CONNECT BY PRIOR id = parent_id START WITH id = ?",
                new Object[]{parentDepartmentId},
                Integer.class
        );
    }

    @Override
    public List<Integer> findAllChildrenIdsByIds(Collection<Integer> parentDepartmentIds) {
        return getJdbcTemplate().queryForList(
                "select distinct id from department CONNECT BY prior id = parent_id " +
                        (parentDepartmentIds.isEmpty() ? "" : "start with " + SqlUtils.transformToSqlInStatement("id", parentDepartmentIds)),
                Integer.class
        );
    }

    @Override
    public List<Integer> fetchAllParentIds(int childDepartmentId) {
        return getJdbcTemplate().queryForList(
                "SELECT id FROM department CONNECT BY PRIOR parent_id = id START WITH id = ?",
                new Object[]{childDepartmentId},
                Integer.class
        );
    }

    @Override
    public List<Department> findAllByIdIn(Collection<Integer> ids) {
        if (!isEmpty(ids)) {
            return getNamedParameterJdbcTemplate().query("select * from department " +
                            "where id in (:ids)",
                    new MapSqlParameterSource("ids", ids),
                    new DepartmentJdbcMapper());
        }
        return new ArrayList<>();
    }

    @Override
    public PagingResult<DepartmentName> searchDepartmentNames(String name, PagingParams pagingParams) {

        // Основной запрос
        StringBuilder mainQuery = new StringBuilder("select id, shortname from department_fullpath");

        // Добавляем поиск по имени
        if (isNotBlank(name)) {
            mainQuery.append(" where lower(shortname) like '%")
                    .append(name.toLowerCase())
                    .append("%'");
        }

        // Используем либо постраничный запрос, либо основной
        StringBuilder query;
        if (pagingParams != null) {
            // Добавляем сортировку к основному запросу
            if (isNotBlank(pagingParams.getProperty()) && isNotBlank(pagingParams.getDirection())) {
                mainQuery.append(" order by ")
                        .append(FormatUtils.convertToUnderlineStyle(pagingParams.getProperty()))
                        .append(" ")
                        .append(pagingParams.getDirection());
            }

            query = new StringBuilder();
            query.append("select * from ( select a.*, rownum rn from (")
                    .append(mainQuery)
                    .append(") a) where rn > ")
                    .append(pagingParams.getStartIndex())
                    .append("and rowNum <= ")
                    .append(pagingParams.getCount());
        } else {
            query = mainQuery;
        }

        // Получаем результат из базы
        List<DepartmentName> departmentNames = getJdbcTemplate().query(
                query.toString(),
                new RowMapper<DepartmentName>() {
                    @Override
                    public DepartmentName mapRow(ResultSet resultSet, int i) throws SQLException {
                        DepartmentName department = new DepartmentName();
                        department.setId(SqlUtils.getInteger(resultSet, "id"));
                        department.setName(resultSet.getString("shortname"));
                        return department;
                    }
                }
        );

        // Запрашиваем полное число найденных объектов
        Integer count = getJdbcTemplate().queryForObject(
                "select count(*) from(" + mainQuery + ")",
                Integer.class
        );

        return new PagingResult<>(departmentNames, count);
    }

    @Override
    public PagingResult<DepartmentShortInfo> fetchAllTBShortInfo(String filter, PagingParams pagingParams) {
        String query = "" +
                "select id, shortname name, is_active active " +
                "from department " +
                "where type = 2";

        if (isNotEmpty(filter)) {
            query = query + "\n" +
                    "and lower(shortname) like '%" + filter.toLowerCase() + "%'";
        }

        Integer count = getJdbcTemplate().queryForObject(
                "select count(*) from(" + query + ")",
                Integer.class
        );

        if (pagingParams != null) {
            String sortProperty = pagingParams.getProperty();
            String sortDirection = pagingParams.getDirection();
            if (isNotBlank(sortProperty) && isNotBlank(sortDirection)) {
                query = query + " order by " + sortProperty + " " + sortDirection;
            }

            query = "select * \n" +
                    "from ( \n" +
                    "   select a.*, rownum rn \n" +
                    "   from ( \n" + query + ") a \n" +
                    ") \n" +
                    "where rn > " + pagingParams.getStartIndex() + " and rownum <= " + pagingParams.getCount();
        }

        List<DepartmentShortInfo> departments = getJdbcTemplate().query(
                query,
                new RowMapper<DepartmentShortInfo>() {
                    @Override
                    public DepartmentShortInfo mapRow(ResultSet resultSet, int i) throws SQLException {
                        DepartmentShortInfo department = new DepartmentShortInfo();
                        department.setId(SqlUtils.getInteger(resultSet, "id"));
                        department.setName(resultSet.getString("name"));
                        department.setActive(resultSet.getBoolean("active"));
                        return department;
                    }
                }
        );

        return new PagingResult<>(departments, count);
    }
}