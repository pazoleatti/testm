package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.TARoleDao;
import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.model.CacheConstants;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

@Repository
public class TAUserDaoImpl extends AbstractDao implements TAUserDao {

    private static final Log LOG = LogFactory.getLog(TAUserDaoImpl.class);

    private static final RowMapper<TAUser> TA_USER_MAPPER = new RowMapper<TAUser>() {
        @Override
        public TAUser mapRow(ResultSet rs, int index) throws SQLException {
            TAUser result = new TAUser();
            result.setId(SqlUtils.getInteger(rs, "id"));
            result.setName(rs.getString("name"));
            result.setActive(SqlUtils.getInteger(rs, "is_active") == 1);
            result.setDepartmentId(SqlUtils.getInteger(rs, "department_id"));
            result.setLogin(rs.getString("login"));
            result.setEmail(rs.getString("email"));
            return result;
        }
    };

    private final RowMapper<TAUserView> TA_USER_VIEW_MAPPER = new RowMapper<TAUserView>() {
        @Override
        public TAUserView mapRow(ResultSet rs, int index) throws SQLException {
            TAUserView result = new TAUserView();
            result.setId(SqlUtils.getInteger(rs, "id"));
            result.setName(rs.getString("name"));
            result.setActive(SqlUtils.getInteger(rs, "active") == 1);
            result.setDepId(SqlUtils.getInteger(rs, "dep_id"));
            result.setLogin(rs.getString("login"));
            result.setEmail(rs.getString("email"));
            result.setRoles(rs.getString("role_names"));
            result.setDepName(rs.getString("path"));
            List<Long> roles = new ArrayList<>();
            for (TARole taRole : getRoles(result.getId(), true)) {
                roles.add((long) taRole.getId());
            }
            result.setTaRoleIds(roles);
            result.setAsnu(rs.getString("asnu_names"));
            result.setAsnuIds(getAsnuIds(result.getId()));
            return result;
        }
    };

    @Override
    @Cacheable(value = CacheConstants.USER, key = "#userId")
    public TAUser getUser(int userId) {
        TAUser user;
        try {
            user = getJdbcTemplate().queryForObject(
                    "select id, login, name, department_id, is_active, email from sec_user where id = ?",
                    new Object[]{userId},
                    new int[]{Types.NUMERIC},
                    TA_USER_MAPPER
            );
            initUser(user);
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException("Пользователь с id = " + userId + " не найден в БД");
        }

        return user;
    }

    @Override
    @Cacheable(value = CacheConstants.USER, key = "'login_'+#login")
    public int getUserIdByLogin(String login) {
        try {
            return getJdbcTemplate().queryForObject("select id from sec_user where lower(login) = lower(?)", new Object[]{login.toLowerCase()}, Integer.class);
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException("Пользователь с login = " + login + " не найден. " + e.toString());
        }
    }

    private List<TARole> getRoles(int userId, boolean onlyNDFL) {
        String tableName = onlyNDFL ? "sec_role_ndfl" : "sec_role";
        return getJdbcTemplate().query(
                "select id, alias, name from " + tableName + " r where exists (select 1 from sec_user_role ur where ur.role_id = r.id and ur.user_id = ?) order by id",
                new Object[]{userId},
                new int[]{Types.NUMERIC},
                TARoleDaoImpl.TA_ROLE_MAPPER
        );
    }

    private List<Long> getAllAsnuIds() {
        return getJdbcTemplate().queryForList(
                "select rba.id asnu_id from ref_book_asnu rba where id <> -1", Long.class);
    }

    private List<Long> getAsnuIds(int userId) {
        return getJdbcTemplate().query(
                "select rba.id asnu_id \n" +
                        "from sec_role r \n" +
                        "join ref_book_asnu rba on rba.role_alias = r.id \n" +
                        "where exists (select 1 from sec_user_role ur where ur.role_id = r.id and ur.user_id = ?) \n" +
                        "order by rba.id",
                new Object[]{userId},
                new int[]{Types.NUMERIC},
                new RowMapper<Long>() {
                    @Override
                    public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return SqlUtils.getLong(rs, "asnu_id");
                    }
                }
        );
    }

    private void initUser(TAUser user) {
        if (user != null) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("User found, login = " + user.getLogin() + ", id = " + user.getId());
            }

            user.setRoles(getRoles(user.getId(), false));
            if (user.hasRoles(TARole.N_ROLE_OPER_ALL, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_CONTROL_UNP)) {
                user.setAsnuIds(getAllAsnuIds());
            } else {
                user.setAsnuIds(getAsnuIds(user.getId()));
            }
        }
    }

    @Override
    public List<Integer> getAllUserIds() {
        try {
            return getJdbcTemplate().queryForList("select id from sec_user", Integer.class);
        } catch (DataAccessException e) {
            throw new DaoException("Ошибка при получении пользователей. " + e.getLocalizedMessage());
        }
    }

    @Override
    public int checkUserRole(String role) {
        return getJdbcTemplate().queryForObject("select count(*) from sec_role where alias=? ", new Object[]{role}, Integer.class);
    }

    @Override
    public List<Integer> getUserIdsByFilter(MembersFilterData filter) {
        if (filter == null) {
            return getAllUserIds();
        }
        StringBuilder sql = new StringBuilder("select id from ( select id, rownum r from (select u.id, u.is_active, u.name, u.department_id " +
                "from sec_user u where 1=1 ");
        if (filter.getActive() != null) {
            sql.append(" and is_active = ")
                    .append(filter.getActive() ? "1" : "0");
        }
        if (filter.getUserName() != null && !filter.getUserName().isEmpty()) {
            sql.append(" and lower(name) like " + "\'%")
                    .append(filter.getUserName().toLowerCase())
                    .append("%\'");
        }
        if (filter.getDepartmentIds() != null && !filter.getDepartmentIds().isEmpty()) {
            sql.append(" and ")
                    .append(SqlUtils.transformToSqlInStatement("department_id", new ArrayList<>(filter.getDepartmentIds())));
        }

        List<Long> roleIds = filter.getRoleIds();

        if (roleIds != null && !roleIds.isEmpty()) {
            sql.append(" and exists (select 1 from sec_user_role ur where u.id = ur.user_id and ")
                    .append(SqlUtils.transformToSqlInStatement("ur.role_id", roleIds)).append(")");
        }
        if (filter.getStartIndex() != null && filter.getCountOfRecords() != null) {
            sql.append(" order by name)) where r between ")
                    .append(filter.getStartIndex() + 1)
                    .append(" and ")
                    .append(filter.getStartIndex() + 1 + filter.getCountOfRecords());
        } else {
            sql.append(" order by name))");
        }
        try {
            return getJdbcTemplate().queryForList(sql.toString(), Integer.class);
        } catch (DataAccessException e) {
            throw new DaoException("Ошибка при получении пользователей. " + e.getLocalizedMessage());
        }
    }

    @Override
    public PagingResult<TAUserView> getUsersViewWithFilter(MembersFilterData filter) {
        StringBuilder sqlForCount = new StringBuilder("select count(*) from (");
        StringBuilder sql = new StringBuilder("select * from ( select rownum r, users.* from (\n");
        sql.append(sqlUserByFilter());

        if (filter == null) {
            sql.append(" order by name ) users)");
            sqlForCount.append(sql.toString())
                    .append(")");
        } else {
            if (filter.getActive() != null) {
                sql.append(" and is_active = ")
                        .append(filter.getActive() ? "1" : "0");
            }
            if (filter.getUserName() != null && !filter.getUserName().isEmpty()) {
                sql.append(" and lower(name) like " + "\'%")
                        .append(filter.getUserName().toLowerCase())
                        .append("%\'");
            }
            if (filter.getDepartmentIds() != null && !filter.getDepartmentIds().isEmpty()) {
                sql.append(" and ")
                        .append(SqlUtils.transformToSqlInStatement("department_id", new ArrayList<>(filter.getDepartmentIds())));
            }

            List<Long> roleIds = filter.getRoleIds();

            if (roleIds != null && !roleIds.isEmpty()) {
                sql.append(" and exists (select 1 from sec_user_role ur where us.id = ur.user_id and ")
                        .append(SqlUtils.transformToSqlInStatement("ur.role_id", roleIds))
                        .append(")");
            }

            sqlForCount
                    .append(sql.toString())
                    .append(") users ))");
            sql.append(" order by ");

            if (filter.getSortField() != null) {
                switch (filter.getSortField()) {
                    case LOGIN:
                        sql.append("login");
                        break;
                    case MAIL:
                        sql.append("email");
                        break;
                    case ACTIVE:
                        sql.append("active");
                        break;
                    case DEPARTMENT:
                        sql.append("path");
                        break;
                    case ROLE:
                        sql.append("role_names");
                        break;
                    case ASNU:
                        sql.append("asnu_names");
                        break;
                    case NAME:
                    default:
                        sql.append("name");
                        break;
                }
            } else {
                sql.append("name");
            }

            sql.append(filter.isAsc() ? " asc" : " desc");
            sql.append(") users) ");
            Integer startIndex = filter.getStartIndex();
            Integer count = filter.getCountOfRecords();
            if (startIndex != null && count != null) {
                sql.append("\nwhere r between ")
                        .append(startIndex + 1)
                        .append(" and ")
                        .append(startIndex + 1 + count);
            }
        }
        try {
            PagingResult<TAUserView> pagingResult = new PagingResult<>(getJdbcTemplate().query(sql.toString(), TA_USER_VIEW_MAPPER));
            pagingResult.setTotalCount(filter == null ? pagingResult.size() : getJdbcTemplate().queryForObject(sqlForCount.toString(), Integer.class));
            return pagingResult;
        } catch (DataAccessException e) {
            throw new DaoException("Ошибка при получении списка пользователей. " + e.getLocalizedMessage());
        }
    }

    private String sqlUserByFilter() {
        return "with \n" +
                "cov_rls as (\n" +
                "   select ur.user_id user_id, sr.name\n" +
                "   from sec_user_role ur\n" +
                "   join sec_role_ndfl sr on ur.role_id=sr.id \n" +
                "   order by user_id\n" +
                "),\n" +
                "arg_rls as (\n" +
                "   select cov_rls.user_id, listagg(cov_rls.name, ', ') within group(order by cov_rls.name) as role_concat\n" +
                "   from cov_rls\n" +
                "   group by cov_rls.user_id\n" +
                "),\n" +
                "cov_asnu as (\n" +
                "   select ur.user_id user_id, r.name \n" +
                "   from sec_role_ndfl r \n" +
                "   join sec_user_role ur on ur.role_id = r.id \n" +
                "   join ref_book_asnu rba on rba.role_alias = r.id\n" +
                "   order by ur.user_id" +
                "),\n" +
                "arg_asnu as (\n" +
                "   select cov_asnu.user_id, listagg(cov_asnu.name, ', ') within group(order by cov_asnu.name) as asnu_concat\n" +
                "   from cov_asnu\n" +
                "   group by cov_asnu.user_id\n" +
                "),\n" +
                "deps as (\n" +
                "   select dep.id as dep_id, ltrim(sys_connect_by_path(dep.shortname, '/'), '/') as path\n" +
                "   from department dep\n" +
                "   start with dep.parent_id in (select id from department where parent_id is null)\n" +
                "   connect by prior dep.id = dep.parent_id\n" +
                "   union\n" +
                "   select dep.id as dep_id, dep.shortname as path\n" +
                "   from department dep\n" +
                "   where parent_id is null" +
                ")\n" +
                "select us.id, us.name, us.login, us.email, us.is_active as active, roless.role_concat as role_names, asnu.asnu_concat as asnu_names, us.department_id as dep_id, deps.path\n" +
                "from sec_user_ndfl us \n" +
                "left join arg_rls roless on roless.user_id=us.id \n" +
                "left join arg_asnu asnu on asnu.user_id=us.id \n" +
                "join deps deps on us.department_id=deps.dep_id";
    }

    @Override
    public int countUsersByFilter(MembersFilterData filter) {
        if (filter == null) {
            return 0;
        }
        StringBuilder sql = new StringBuilder("select count(*) from (select  u.*, rownum r from sec_user u where 1=1 ");
        if (filter.getActive() != null) {
            sql.append(" and is_active = ")
                    .append(filter.getActive() ? "1" : "0");
        }
        if (filter.getUserName() != null && !filter.getUserName().isEmpty()) {
            sql.append(" and name like " + "\'%")
                    .append(filter.getUserName())
                    .append("%\'");
        }
        if (filter.getDepartmentIds() != null && !filter.getDepartmentIds().isEmpty()) {
            sql.append(" and ")
                    .append(SqlUtils.transformToSqlInStatement("department_id", new ArrayList<>(filter.getDepartmentIds())));
        }

        List<Long> roleIds = filter.getRoleIds();

        if (roleIds != null && !roleIds.isEmpty() && roleIds.get(0) != null) {
            sql.append(" and exists (select 1 from sec_user_role ur where u.id = ur.user_id and ur.role_id = ")
                    .append(roleIds.get(0))
                    .append(")");
        }
        sql.append(")");
        try {
            return getJdbcTemplate().queryForObject(sql.toString(), Integer.class);
        } catch (DataAccessException e) {
            throw new DaoException("Ошибка при получении кол-ва пользователей. " + e.getLocalizedMessage());
        }
    }

    @Override
    public boolean existsUser(String login) {
        // Проверяем, что с таким логином ровно один пользователь
        String sql = "select count(id) from sec_user where lower(login) = ?";
        Integer result = getJdbcTemplate().queryForObject(sql, new Object[]{login.toLowerCase()}, Integer.class);
        return (result == 1);
    }

    @Override
    public SecuredEntity findSecuredEntityById(long id) {
        return getUser((int) id);
    }
}
