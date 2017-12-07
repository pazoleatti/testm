package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.impl.cache.CacheConstants;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentType;
import com.aplana.sbrf.taxaccounting.model.SecuredEntity;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.sql.SQLQueryFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static com.aplana.sbrf.taxaccounting.model.querydsl.QDepartment.department;
import static com.aplana.sbrf.taxaccounting.model.querydsl.QDepartmentChildView.departmentChildView;
import static com.aplana.sbrf.taxaccounting.model.querydsl.QDepartmentDeclTypePerformer.departmentDeclTypePerformer;
import static com.aplana.sbrf.taxaccounting.model.querydsl.QDepartmentDeclarationType.departmentDeclarationType;

@Repository
@Transactional(readOnly = true)
public class DepartmentDaoImpl extends AbstractDao implements DepartmentDao {

    private static final Log LOG = LogFactory.getLog(DepartmentDaoImpl.class);

    final private SQLQueryFactory sqlQueryFactory;

    public DepartmentDaoImpl(SQLQueryFactory sqlQueryFactory) {
        this.sqlQueryFactory = sqlQueryFactory;
    }

    @Override
    @Cacheable(CacheConstants.DEPARTMENT)
    public Department getDepartment(int id) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Fetching department with id = " + id + " from database");
        }
        try {
            return getJdbcTemplate().queryForObject(
                    "select * from department where id = ?",
                    new Object[]{id},
                    new DepartmentJdbcMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException("Не удалось найти подразделение банка с id = " + id);
        }
    }

    @Override
    public boolean existDepartment(int id) {
        return getJdbcTemplate().queryForObject("select count(id) from department where id = ?", new Object[]{id}, Integer.class) == 1;
    }

    @Override
    public List<Department> getChildren(int parentDepartmentId) {
        try {
            return getJdbcTemplate().query(
                    "select * from department where parent_id = ?",
                    new Object[]{parentDepartmentId},
                    new DepartmentJdbcMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Department>(0);
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
        return "SELECT LTRIM(SYS_CONNECT_BY_PATH(CASE WHEN " + columnName + " is not null THEN " + columnName + " ELSE name END, '/'), '/') as path \n" +
                "FROM department\n" +
                "WHERE id = ? \n" +
                "START WITH parent_id in (select id from department where parent_id is null)  \n" +
                "CONNECT BY PRIOR id = parent_id";
    }

    @Override
    public Integer getParentTBId(int departmentId) {
        try {
            return getJdbcTemplate().queryForObject("SELECT id FROM department WHERE parent_id = 0 and type = 2 " +
                    "START WITH id = ? CONNECT BY id = prior parent_id", new Object[]{departmentId}, Integer.class);
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
            return getJdbcTemplate().queryForObject("SELECT * FROM department WHERE parent_id = 0 and type = 2 " +
                    "START WITH id = ? CONNECT BY id = prior parent_id", new Object[]{departmentId}, new DepartmentJdbcMapper());
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
                    "select * from department",
                    new DepartmentJdbcMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Department>(0);
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
                department.setId(SqlUtils.getInteger(rs, "id"));
                department.setName(rs.getString("name"));
                Integer parentId = SqlUtils.getInteger(rs, "parent_id");
                // В ResultSet есть особенность что если пришло значение нул то вернет значение по умолчанию - то есть для Integer'a вернет 0
                // а так как у нас в базе 0 используется в качестве идентификатора то нужно null нужно првоерять через .wasNull()
                department.setParentId(rs.wasNull() ? null : parentId);
                department.setType(DepartmentType.fromCode(SqlUtils.getInteger(rs, "type")));
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
                    "SELECT * FROM department dp WHERE lower(dp.sbrf_code) = lower(?) and (? = 0 or dp.is_active = ?)",
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
                "SELECT * FROM department dp WHERE lower(dp.sbrf_code) = lower(?) and (? = 0 or dp.is_active = ?)",
                new Object[]{sbrfCode, activeOnly ? 1 : 0, activeOnly ? 1 : 0},
                new DepartmentJdbcMapper()
        );
    }

    @Override
    public Department getDepartmentByName(String name) {
        return getJdbcTemplate().queryForObject(
                "SELECT * FROM department dp WHERE dp.name = ?",
                new Object[]{name},
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
            return new ArrayList<Department>(0);
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
            return new ArrayList<Integer>(0);
        }
    }

    @Override
    public Department getDepartmentTB(int departmentId) {
        return getParentDepartmentByType(departmentId, 2);
    }

    @Override
    public Department getParentDepartmentByType(int departmentId, DepartmentType type) {
        return getParentDepartmentByType(departmentId, type.getCode());
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
            return new ArrayList<Department>(0);
        }
    }

    /**
     * Получение родительского узла заданного типа (указанное подразделение м.б. результатом, если его тип соответствует искомому)
     */
    private Department getParentDepartmentByType(int departmentId, int typeId) {
        try {
            String recursive = isWithRecursive() ? "recursive" : "";
            return getJdbcTemplate().queryForObject("with " + recursive + " tree (id, parent_id, type) as " +
                            "(select id, parent_id, type from department where id = ? " +
                            "union all select d.id, d.parent_id, d.type from department d " +
                            "inner join tree t on d.id = t.parent_id) " +
                            "select d.* from department d, tree where d.id = tree.id and tree.type = ?",
                    new Object[]{departmentId, typeId},
                    new DepartmentJdbcMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
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
            return new ArrayList<Department>(0);
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
            return new ArrayList<Integer>(0);
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
                    "select * from department CONNECT BY prior id = parent_id start with id = ?",
                    new Object[]{parentDepartmentId},
                    new DepartmentJdbcMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Department>(0);
        }
    }

    @Override
    public List<Integer> getDepartmentIdsByExecutors(List<Integer> departments) {
        String sql = String.format("select distinct department_id from department_declaration_type ddt \n" +
                "left join declaration_type dt on ddt.declaration_type_id = dt.id \n" +
                "left join department_decl_type_performer ddtp on ddtp.department_decl_type_id = dt.id \n" +
                "where %s ", SqlUtils.transformToSqlInStatement("ddtp.performer_dep_id", departments));

        return getJdbcTemplate().queryForList(sql, Integer.class);
    }

    @Override
    @Transactional(readOnly = false)
    public void setUsedByGarant(int depId, boolean used) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Updating usage department by Garant with id = " + depId + " to value = " + used);
        }
        try {
            int usedInt = used ? 1 : 0;
            getJdbcTemplate().update(
                    "update department set garant_use = ? where id = ?", usedInt, depId);
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException("Не удалось найти подразделение банка с id = " + depId);
        }
    }

    @Override
    public int getHierarchyLevel(int departmentId) {
        try {
            return getJdbcTemplate().queryForObject("SELECT level " +
                    "FROM department " +
                    "WHERE id = " + departmentId + " " +
                    "START WITH parent_id is null " +
                    "CONNECT BY PRIOR id = parent_id", Integer.class);
        } catch (EmptyResultDataAccessException e) {
            return 0;
        }
    }

    @Override
    public List<Integer> getAllPerformers(int userDepId, int declarationTypeId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userDepId", userDepId);
        params.put("declarationTypeId", declarationTypeId);
        try {
            return getNamedParameterJdbcTemplate().queryForList("SELECT ddt.department_id\n" +
                    "FROM department_declaration_type ddt\n" +
                    "WHERE ddt.declaration_type_id = :declarationTypeId AND ddt.department_id IN (\n" +
                    "  SELECT dep.ID FROM department dep CONNECT BY PRIOR dep.ID = dep.parent_id START WITH dep.ID in (\n" +
                    "    SELECT DISTINCT ddt.department_id\n" +
                    "    FROM department_declaration_type ddt\n" +
                    "    INNER JOIN department_decl_type_performer ddtp ON ddt.ID = ddtp.department_decl_type_id \n" +
                    "    WHERE ddt.declaration_type_id = :declarationTypeId AND ddtp.performer_dep_id IN (SELECT dep_ddtp.ID FROM department dep_ddtp CONNECT BY PRIOR dep_ddtp.ID = dep_ddtp.parent_id START WITH dep_ddtp.ID = :userDepId)\n" +
                    "  )\n" +
                    ")", params, Integer.class);
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Integer>();
        }
    }

    @Override
    public List<Integer> getTBDepartmentIdsByDeclarationPerformer(int performerDepartmentId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("performerDepartmentId", performerDepartmentId);
        try {
            return getNamedParameterJdbcTemplate().queryForList("SELECT id\n" +
                    "FROM department\n" +
                    "WHERE parent_id = 0 AND type = 2\n" +
                    "CONNECT BY id = prior parent_id START WITH id IN (\n" +
                    "  SELECT DISTINCT ddt.department_id\n" +
                    "  FROM department_declaration_type ddt\n" +
                    "  INNER JOIN department_decl_type_performer ddtp ON ddt.id = ddtp.department_decl_type_id\n" +
                    "  WHERE ddtp.performer_dep_id in (\n" +
                    "    select dep_ddtp.id from department dep_ddtp connect by prior dep_ddtp.id = dep_ddtp.parent_id start with dep_ddtp.id = :performerDepartmentId\n" +
                    "  )" +
                    ")", params, Integer.class);
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Integer>();
        }
    }

    @Override
    public List<Integer> getAllTBPerformers(int userDepId, int declarationTypeId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userDepId", userDepId);
        params.put("declarationTypeId", declarationTypeId);
        try {
            return getNamedParameterJdbcTemplate().queryForList("SELECT id\n" +
                    "FROM department\n" +
                    "WHERE parent_id = 0 AND type = 2\n" +
                    "CONNECT BY id = prior parent_id START WITH id IN (\n" +
                    "  SELECT DISTINCT ddt.department_id\n" +
                    "  FROM department_declaration_type ddt\n" +
                    "  INNER JOIN department_decl_type_performer ddtp ON ddt.id = ddtp.department_decl_type_id\n" +
                    "  where ddt.declaration_type_id=:declarationTypeId and ddtp.performer_dep_id in (\n" +
                    "    select dep_ddtp.id from department dep_ddtp connect by prior dep_ddtp.id = dep_ddtp.parent_id start with dep_ddtp.id = :userDepId\n" +
                    "  )\n" +
                    ")", params, Integer.class);
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Integer>();
        }
    }

    @Override
    public String getDepartmentNameByPairKppOktmo(String kpp, String oktmo, Date reportPeriodEndDate) {
        String query = "select dep.name, max(rnd.version) " +
                "from department dep " +
                "join ref_book_ndfl_detail rnd on dep.id = rnd.department_id " +
                "join ref_book_oktmo ro on ro.id = rnd.oktmo " +
                "where rnd.kpp = :kpp and ro.code = :oktmo and rnd.version <= :reportPeriodEndDate group by dep.NAME";
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
        if (result != null) {
            return result.get(0);
        }
        return "";
    }

    @Override
    public List<Integer> listDepartmentIds() {
        return sqlQueryFactory
                .select(department.id)
                .from(department)
                .fetch();
    }

    @Override
    public List<Integer> getAllChildrenIds(int parentDepartmentId) {
        //В department_child_view, в отличие от начального запроса select id from department CONNECT BY prior id = parent_id start with id = ?
        //подразделение не является родителем (потомком) самого себя, поэтому его тоже нужно добавить в список. Но нужно убедиться, что
        //подразделение существует в БД

        SubQueryExpression<Integer> childrenQuery = sqlQueryFactory
                .select(departmentChildView.id)
                .from(departmentChildView)
                .where(departmentChildView.parentId.eq(parentDepartmentId));

        SubQueryExpression<Integer> departmentQuery = sqlQueryFactory
                .select(department.id)
                .from(department)
                .where(department.id.eq(parentDepartmentId));

        return sqlQueryFactory
                .query()
                .union(childrenQuery, departmentQuery)
                .fetch();
    }

    @Override
    public List<Integer> getAllChildrenIds(List<Integer> parentDepartmentIds) {
        //В department_child_view, в отличие от начального запроса select id from department CONNECT BY prior id = parent_id start with id = ?
        //подразделение не является родителем (потомком) самого себя, поэтому его тоже нужно добавить в список. Но нужно убедиться, что
        //подразделение существует в БД

        SubQueryExpression<Integer> childrenQuery = sqlQueryFactory
                .select(departmentChildView.id)
                .from(departmentChildView)
                .where(departmentChildView.parentId.in(parentDepartmentIds));

        SubQueryExpression<Integer> departmentQuery = sqlQueryFactory
                .select(department.id)
                .from(department)
                .where(department.id.in(parentDepartmentIds));

        return sqlQueryFactory
                .query()
                .union(childrenQuery, departmentQuery)
                .fetch();
    }

    @Override
    public List<Integer> fetchAllParentDepartmentsIds(int childDepartmentId) {
        //В department_child_view подразделение не является родителем (потомком) самого себя, поэтому его тоже нужно
        // добавить в список. Но нужно убедиться, что подразделение существует в БД

        SubQueryExpression<Integer> parentsQuery = sqlQueryFactory
                .select(departmentChildView.parentId)
                .from(departmentChildView)
                .where(departmentChildView.id.eq(childDepartmentId).and(departmentChildView.parentId.isNotNull()));

        SubQueryExpression<Integer> departmentQuery = sqlQueryFactory
                .select(department.id)
                .from(department)
                .where(department.id.eq(childDepartmentId));

        return sqlQueryFactory
                .query()
                .union(parentsQuery, departmentQuery)
                .fetch();
    }

    @Override
    public List<Integer> getDepartmentsByDeclarationsPerformers(List<Integer> performersIds) {
        return sqlQueryFactory
                .select(departmentDeclarationType.departmentId)
                .distinct()
                .from(departmentDeclarationType).innerJoin(departmentDeclTypePerformer).on(departmentDeclarationType.id.eq(departmentDeclTypePerformer.departmentDeclTypeId))
                .where(departmentDeclTypePerformer.performerDepId.in(performersIds))
                .fetch();
    }
}