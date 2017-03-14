package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

@Repository
public class TAUserDaoImpl extends AbstractDao implements TAUserDao {

	private static final Log LOG = LogFactory.getLog(TAUserDaoImpl.class);
	
	private static final RowMapper<TARole> TA_ROLE_MAPPER = new RowMapper<TARole>() {
		
		@Override
		public TARole mapRow(ResultSet rs, int index) throws SQLException {
			TARole result = new TARole();
			result.setId(SqlUtils.getInteger(rs,"id"));
			result.setName(rs.getString("name"));
			result.setAlias(rs.getString("alias"));
            result.setTaxType(TaxType.fromCode(rs.getString("tax_type").charAt(0)));
            return result;
		}
	}; 
	
	private static final RowMapper<TAUser> TA_USER_MAPPER = new RowMapper<TAUser>() {
		
		@Override
		public TAUser mapRow(ResultSet rs, int index) throws SQLException {
			TAUser result = new TAUser();
			result.setId(SqlUtils.getInteger(rs,"id"));
			result.setName(rs.getString("name"));
			result.setActive(SqlUtils.getInteger(rs,"is_active") == 1);
			result.setDepartmentId(SqlUtils.getInteger(rs,"department_id"));
			result.setLogin(rs.getString("login"));
			result.setEmail(rs.getString("email"));
			return result;
		}
	};

    private final RowMapper<TAUserView> TA_USER_VIEW_MAPPER = new RowMapper<TAUserView>() {

        @Override
        public TAUserView mapRow(ResultSet rs, int index) throws SQLException {
            TAUserView result = new TAUserView();
            result.setId(SqlUtils.getInteger(rs,"id"));
            result.setName(rs.getString("name"));
            result.setActive(SqlUtils.getInteger(rs, "active") == 1);
            result.setDepId(SqlUtils.getInteger(rs, "dep_id"));
            result.setLogin(rs.getString("login"));
            result.setEmail(rs.getString("email"));
            result.setRoles(rs.getString("role_names"));
            result.setDepName(rs.getString("path"));
            List<Long> roles = new ArrayList<Long>();
            for(TARole taRole: getRoles(result.getId())) {
                roles.add((long)taRole.getId());
            }
            result.setTaRoleIds(roles);
            result.setAsnu(rs.getString("asnu_names"));
            result.setAsnuIds(getAsnuIds(result.getId()));
            return result;
        }
    };

	@Override
	@Cacheable(value = "User", key = "#userId")
	public TAUser getUser(int userId) {
		TAUser user;
		try {
			user = getJdbcTemplate().queryForObject(
				"select id, login, name, department_id, is_active, email from sec_user where id = ?",
				new Object[] { userId },
				new int[] { Types.NUMERIC },
				TA_USER_MAPPER
			);
			initUser(user);
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException("Пользователь с id = " + userId + " не найден в БД");
		}

		return user;
	}
	
	@Override
    @Cacheable(value = "User", key = "'login_'+#login")
	public int getUserIdByLogin(String login) {
		try {
			return getJdbcTemplate().queryForObject("select id from sec_user where lower(login) = lower(?)", new Object[] {login.toLowerCase()}, Integer.class);
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException("Пользователь с login = " + login + " не найден. " + e.toString());
		}
	}

    private List<TARole> getRoles(int userId) {
        return getJdbcTemplate().query(
                "select id, alias, name, tax_type from sec_role r where exists (select 1 from sec_user_role ur where ur.role_id = r.id and ur.user_id = ?) order by id",
                new Object[] { userId },
                new int[] { Types.NUMERIC },
                TA_ROLE_MAPPER
        );
    }

    private List<Long> getAsnuIds(int userId) {
        return getJdbcTemplate().query(
                "select asnu_id from sec_user_asnu sua where sua.user_id = ?",
                new Object[] { userId },
                new int[] { Types.NUMERIC },
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

			user.setRoles(getRoles(user.getId()));
            user.setAsnuIds(getAsnuIds(user.getId()));
        }
	}

	@Override
	public int createUser(final TAUser user) {
		final KeyHolder keyHolder = new GeneratedKeyHolder();
		try {
			PreparedStatementCreator psc = new PreparedStatementCreator() {
				
				@Override
				public PreparedStatement createPreparedStatement(Connection con)
						throws SQLException {

					//prepareStatement(sql, string_field) Only for ORACLE using
					PreparedStatement ps = con
							.prepareStatement(
									"insert into sec_user (id, name, login, department_id, is_active, email) values (seq_sec_user.nextval,?,?,?,?,?)",
									new String[]{"ID"});
					ps.setString(1, user.getName());
					ps.setString(2, user.getLogin());
					ps.setInt(3, user.getDepartmentId());
					ps.setBoolean(4, user.isActive());
					ps.setString(5, user.getEmail());
					return ps;
				}
			}; 
			getJdbcTemplate().update(psc, keyHolder);

			getJdbcTemplate().batchUpdate("insert into sec_user_role (user_id, role_id) " +
					"select ?, id from sec_role where alias = ?",new BatchPreparedStatementSetter() {
				
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					TARole role = user.getRoles().get(i);
					ps.setInt(1, keyHolder.getKey().intValue());
					ps.setString(2, role.getAlias());
				}
				
				@Override
				public int getBatchSize() {
					return user.getRoles().size();
				}
			});
            user.setId(keyHolder.getKey().intValue());
            updateUserAsnu(user);
            return keyHolder.getKey().intValue();
		} catch (DataAccessException e) {
			throw new DaoException("Пользователя с login = " + user.getLogin() + " не удалось сохранить." + e.getLocalizedMessage());
		}
	}

	@Override
	@CacheEvict(value="User", key="#userId", beforeInvocation=true)
	public void setUserIsActive(int userId, int isActive) {
		int rows = getJdbcTemplate().update("update sec_user set is_active=? where id = ?", 
				new Object[]{isActive, userId},
				new int[]{Types.NUMERIC, Types.NUMERIC});
		if(rows == 0)
			throw new DaoException("Пользователя с id = " + userId + " не существует. Не удалось выставить флаг active.");
	}

	@Override
	@CacheEvict(value="User", key="#user.id", beforeInvocation=true)
	public void updateUser(final TAUser user) {
		try {
			List<Object> array = new ArrayList<Object>();
			StringBuilder sb = new StringBuilder("update sec_user set");
			if(user.getDepartmentId() != 0){
				sb.append(" department_id=?,");
				array.add(user.getDepartmentId());
			}	
			if(user.getEmail() != null){
				sb.append(" email=?,");
				array.add(user.getEmail());
			}	
			if(user.getName() != null){
				sb.append(" name=?,");
				array.add(user.getName());
			}
			sb.deleteCharAt(sb.toString().trim().length() - 1); //delete separator
			sb.append(" where lower(login) = ?");
			array.add(user.getLogin().toLowerCase());
			int rows = getJdbcTemplate().update(sb.toString(),	array.toArray());
			if(rows == 0)
				throw new DaoException("Пользователя с login = " + user.getLogin() + " не существует.");
			LOG.debug("Update user meta info " + user);
		} catch (DataAccessException e) {
			throw new DaoException("Не удалось обновить метаинформацию о пользователе с login = " + user.getLogin() + "."
            + e.getLocalizedMessage());
		}
       	updateUserRoles(user);
        updateUserAsnu(user);
	}
	
	private void updateUserRoles(final TAUser user) {
		try {
            getJdbcTemplate().update("delete from sec_user_role where user_id=" +
                    "(select id from sec_user where lower(login)=?)",user.getLogin().toLowerCase());

			getJdbcTemplate().batchUpdate("insert into sec_user_role (user_id, role_id) " +
					"select ?, id from sec_role where alias = ?",
					new BatchPreparedStatementSetter() {
				
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					TARole role = user.getRoles().get(i);
					ps.setInt(1, user.getId());
					ps.setString(2, role.getAlias());
				}
				
				@Override
				public int getBatchSize() {
					return user.getRoles().size();
				}
			});
			LOG.debug("User update roles success " + user);
		} catch (DataAccessException e) {
			throw new DaoException("Не удалось обновить роли для пользователя с login = " + user.getLogin() + "." + e.getLocalizedMessage());
		}
	}

    private void updateUserAsnu(final TAUser user) {
        try {
            getJdbcTemplate().update("delete from sec_user_asnu where user_id=?", user.getId());

            if (!user.getAsnuIds().isEmpty()) {
                getJdbcTemplate().batchUpdate("insert into sec_user_asnu (user_id, asnu_id, id) values " +
                                "(?, ?, seq_sec_user_asnu_id.nextval)",
                        new BatchPreparedStatementSetter() {
                            @Override
                            public void setValues(PreparedStatement ps, int i) throws SQLException {
                                Long asnuId = user.getAsnuIds().get(i);
                                ps.setInt(1, user.getId());
                                ps.setLong(2, asnuId);
                            }

                            @Override
                            public int getBatchSize() {
                                return user.getAsnuIds().size();
                            }
                        });
            }
            LOG.debug("User update roles success " + user);
        } catch (DataAccessException e) {
            throw new DaoException("Не удалось обновить АСНУ для пользователя с login = " + user.getLogin() + "." + e.getLocalizedMessage(), e);
        }
    }
	@Override
	public List<Integer> getUserIds() {
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
	public List<Integer> getByFilter(MembersFilterData filter) {
		if (filter == null) {
			return getUserIds();
		}
		StringBuilder sql = new StringBuilder("select id from ( select id, rownum r from (select u.id, u.is_active, u.name, u.department_id " +
				"from sec_user u where 1=1 ");
		if (filter.getActive() != null) {
			sql.append(" and is_active = ").append(filter.getActive() ? "1" : "0");
		}
		if (filter.getUserName() != null && !filter.getUserName().isEmpty()) {
			sql.append(" and lower(name) like " + "\'%").append(filter.getUserName().toLowerCase()).append("%\'");
		}
		if (filter.getDepartmentIds() != null && !filter.getDepartmentIds().isEmpty()) {
			sql.append(" and ").append(SqlUtils.transformToSqlInStatement("department_id", new ArrayList<Integer>(filter.getDepartmentIds())));
		}
		if (filter.getRoleIds() != null && !filter.getRoleIds().isEmpty()) {
			sql.append(" and exists (select 1 from sec_user_role ur where u.id = ur.user_id and ").append(SqlUtils.transformToSqlInStatement("ur.role_id", filter.getRoleIds())).append(")");
		}
		if (filter.getStartIndex() != null && filter.getCountOfRecords() != null) {
			sql.append(" order by name)) where r between ").append(filter.getStartIndex() + 1).append(" and ").append(filter.getStartIndex() + 1 + filter.getCountOfRecords());
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
    public PagingResult<TAUserView> getUsersByFilter(MembersFilterData filter) {
        StringBuilder sqlForCount = new StringBuilder("select count(*) from (");
        StringBuilder sql = new StringBuilder("select * from ( select rownum r, users.* from (\n");
        sql.append(sqlUserByFilter());

        if (filter == null) {
            sql.append(" order by name ) users)");
            sqlForCount.append(sql.toString()).append(")");
        } else {
            if (filter.getActive() != null) {
                sql.append(" and is_active = ").append(filter.getActive() ? "1" : "0");
            }
            if (filter.getUserName() != null && !filter.getUserName().isEmpty()) {
                sql.append(" and lower(name) like " + "\'%").append(filter.getUserName().toLowerCase()).append("%\'");
            }
            if (filter.getDepartmentIds() != null && !filter.getDepartmentIds().isEmpty()) {
                sql.append(" and ").append(SqlUtils.transformToSqlInStatement("department_id", new ArrayList<Integer>(filter.getDepartmentIds())));
            }
            if (filter.getRoleIds() != null && !filter.getRoleIds().isEmpty()) {
                sql.append(" and exists (select 1 from sec_user_role ur where us.id = ur.user_id and ").append(SqlUtils.transformToSqlInStatement("ur.role_id", filter.getRoleIds())).append(")");
            }

            sqlForCount.append(sql.toString()).append(") users ))");
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
                sql.append("\nwhere r between ").append(startIndex + 1).append(" and ").append(startIndex + 1 + count);
            }
        }
        try {
            PagingResult<TAUserView> pagingResult = new PagingResult<TAUserView>(getJdbcTemplate().query(sql.toString(), TA_USER_VIEW_MAPPER));
            pagingResult.setTotalCount(filter == null ? pagingResult.size() : getJdbcTemplate().queryForObject(sqlForCount.toString(), Integer.class));
            return pagingResult;
        } catch (DataAccessException e) {
            throw new DaoException("Ошибка при получении списка пользователей. " + e.getLocalizedMessage());
        }
    }

    private String sqlUserByFilter(){
        return "with \n" +
                "cov_rls as (\n" +
                    "select ur.user_id user_id, sr.name\n" +
                    "from sec_user_role ur\n" +
                    "join sec_role sr on ur.role_id=sr.id \n" +
                    "order by user_id\n" +
                "),\n" +
                "arg_rls as (\n" +
                    "select cov_rls.user_id, listagg(cov_rls.name, ', ') within group(order by cov_rls.name) as role_concat\n" +
                    "from cov_rls\n" +
                    "group by cov_rls.user_id\n" +
                "),\n" +
                "cov_asnu as (\n" +
                    "select ua.user_id user_id, rba.name\n" +
                    "from sec_user_asnu ua\n" +
                    "join ref_book_asnu rba on ua.asnu_id=rba.id \n" +
                    "order by user_id\n" +
                "),\n" +
                "arg_asnu as (\n" +
                    "select cov_asnu.user_id, listagg(cov_asnu.name, ', ') within group(order by cov_asnu.name) as asnu_concat\n" +
                    "from cov_asnu\n" +
                    "group by cov_asnu.user_id\n" +
                "),\n" +
                "deps as (\n" +
                    "select dep.id as dep_id, ltrim(sys_connect_by_path(dep.shortname, '/'), '/') as path\n" +
                    "from department dep\n" +
                    "start with dep.parent_id in (select id from department where parent_id is null)\n" +
                    "connect by prior dep.id = dep.parent_id\n" +
                    "union\n" +
                    "select dep.id as dep_id, dep.shortname as path\n" +
                    "from department dep\n" +
                    "where parent_id is null" +
                ")\n" +
                "select us.id, us.name, us.login, us.email, us.is_active as active, roless.role_concat as role_names, asnu.asnu_concat as asnu_names, us.department_id as dep_id, deps.path\n" +
                "from sec_user us \n" +
                "join arg_rls roless on roless.user_id=us.id \n" +
                "left join arg_asnu asnu on asnu.user_id=us.id \n" +
                "join deps deps on us.department_id=deps.dep_id";
    }

    @Override
	public int count(MembersFilterData filter) {
		if (filter == null) {
			return 0;
		}
		StringBuilder sql = new StringBuilder("select count(*) from (select  u.*, rownum r from sec_user u where 1=1 ");
		if (filter.getActive() != null) {
			sql.append(" and is_active = ").append(filter.getActive() ? "1" : "0");
		}
		if (filter.getUserName() != null && !filter.getUserName().isEmpty()) {
			sql.append(" and name like " + "\'%").append(filter.getUserName()).append("%\'");
		}
		if (filter.getDepartmentIds() != null && !filter.getDepartmentIds().isEmpty()) {
			sql.append(" and ").append(SqlUtils.transformToSqlInStatement("department_id", new ArrayList<Integer>(filter.getDepartmentIds())));
		}
		if (filter.getRoleIds() != null && !filter.getRoleIds().isEmpty() && filter.getRoleIds().get(0) != null) {
			sql.append(" and exists (select 1 from sec_user_role ur where u.id = ur.user_id and ur.role_id = ").append(filter.getRoleIds().get(0)).append(")");
		}
		sql.append(")");
		try {
			return getJdbcTemplate().queryForObject(sql.toString(), Integer.class);
		} catch (DataAccessException e) {
			throw new DaoException("Ошибка при получении кол-ва пользователей. " + e.getLocalizedMessage());
		}
	}

	/**
	 * Проверяет, есть ли пользователь с таким логином.
	 *
	 * @param login проверяемый логин пользователя
	 * @return true если пользователь с таким логином есть, false если нет
	 */
	@Override
	public boolean existsUser(String login) {
		return getJdbcTemplate().queryForObject("select count(id) from sec_user where lower(login) = ?", new Object[]{login.toLowerCase()}, Integer.class) == 1;
	}
}
