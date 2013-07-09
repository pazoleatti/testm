package com.aplana.sbrf.taxaccounting.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

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
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;

@Repository
@Transactional(readOnly=true)
public class TAUserDaoImpl extends AbstractDao implements TAUserDao {
	private final Log logger = LogFactory.getLog(getClass());
	
	private static final RowMapper<TARole> TA_ROLE_MAPPER = new RowMapper<TARole>() {
		
		@Override
		public TARole mapRow(ResultSet rs, int index) throws SQLException {
			TARole result = new TARole();
			result.setId(rs.getInt("id"));
			result.setName(rs.getString("name"));
			result.setAlias(rs.getString("alias"));
			return result;
		}
	}; 
	
	private static final RowMapper<TAUser> TA_USER_MAPPER = new RowMapper<TAUser>() {
		
		@Override
		public TAUser mapRow(ResultSet rs, int index) throws SQLException {
			TAUser result = new TAUser();
			result.setId(rs.getInt("id"));
			result.setName(rs.getString("name"));
			result.setActive(rs.getBoolean("is_active"));
			result.setDepartmentId(rs.getInt("department_id"));
			result.setLogin(rs.getString("login"));
			result.setEmail(rs.getString("email"));
			return result;
		}
	};

	@Override
	@Cacheable("User")
	public TAUser getUser(int userId) {
		TAUser user;
		try {
			user = getJdbcTemplate().queryForObject(
				"select * from sec_user where id = ?",
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
	public int getUserIdbyLogin(String login) {
		try {
			return getJdbcTemplate().queryForInt("select id from sec_user where login = ?", login);
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException("Пользователь с login = " + login + " не найден. " + e.toString());
		}
		
	}
	
	private void initUser(TAUser user) {
		if (user != null) {
			if (logger.isTraceEnabled()) {
				logger.trace("User found, login = " + user.getLogin() + ", id = " + user.getId());
			}
			List<TARole> userRoles = getJdbcTemplate().query(
				"select * from sec_role r where exists (select 1 from sec_user_role ur where ur.role_id = r.id and ur.user_id = ?)",
				new Object[] { user.getId() },
				new int[] { Types.NUMERIC },
				TA_ROLE_MAPPER
			);
			user.setRoles(userRoles);
		}
	}

	@Transactional(readOnly=false)
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
			/*PreparedStatementCreator psc1 = new PreparedStatementCreator() {
				
				@Override
				public PreparedStatement createPreparedStatement(Connection con)
						throws SQLException {
					
					//prepareStatement(sql, string_field) Only for ORACLE using
					PreparedStatement ps = con
							.prepareStatement(
									"insert into sec_user_role (user_id, role_id) " +
											"select ?, id from sec_role where alias = ?");
					ps.setInt(1, keyHolder.getKey().intValue());
					ps.setString(2, user.getRoles().get(0).getAlias());
					return ps;
				}
			}; 
			getJdbcTemplate().update(psc1);*/
			
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
			return keyHolder.getKey().intValue();
		} catch (DataAccessException e) {
			throw new DaoException("Пользователя с login = " + user.getLogin() + " не удалось сохранить." + e.toString());
		} 
		
	}

	@Transactional(readOnly=false)
	@Override
	@CacheEvict(value="User", key="#userId", beforeInvocation=true)
	public void setUserIsActive(int userId, boolean isActive) {
		int rows = getJdbcTemplate().update("update sec_user set is_active=? where id = ?", 
				new Object[]{isActive, userId},
				new int[]{Types.BOOLEAN, Types.INTEGER});
		if(rows == 0)
			throw new DaoException("Пользователя с id = " + userId + " не существует. Не удалось выставить флаг active.");
		
	}

	@Transactional(readOnly=false)
	@Override
	@CacheEvict(value="User", key="#user.id", beforeInvocation=true)
	public void updateUser(final TAUser user) throws DaoException {
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
			sb.append(" where login = ?");
			array.add(user.getLogin());
			int rows = getJdbcTemplate().update(sb.toString(),	array.toArray());
			if(rows == 0)
				throw new DaoException("Пользователя с login = " + user.getLogin() + " не существует.");
			logger.debug("Update user meta info " + user);
			
			
		} catch (DataAccessException e) {
			throw new DaoException("Не удалось обновить метаинформацию о пользователе с login = " + user.getLogin());
		}
		if(user.getRoles().size() != 0)
			updateUserRoles(user);
		
	}
	
	private void updateUserRoles(final TAUser user) {
		try {
			/*final int userRolesCount = getJdbcTemplate().queryForInt(
					"select count(*) from sec_user_role ur where ur.user_id = " +
					"(select id from sec_user where login=?)", user.getLogin()
				);*/
            getJdbcTemplate().update("delete from sec_user_role where user_id=" +
                    "(select id from sec_user where login=?)",user.getLogin());

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
			logger.debug("User update roles success " + user);
		} catch (DataAccessException e) {
			throw new DaoException("Не удалось обновить роли для пользователя с login = " + user.getLogin());
		}
		
	}

	@Override
	public List<Integer> getUserIds() {
		try {
			return getJdbcTemplate().queryForList("select id from sec_user", Integer.class);
		} catch (DataAccessException e) {
			throw new DaoException("Ошибка при получении пользователей. ");
		}
	}

	@Override
	public int checkUserRole(String role) {
		return getJdbcTemplate().queryForInt("select count(*) from sec_role where alias=? ", role);
	}

	@Override
	public int checkUserLogin(String login) {
		 List<Integer> list = getJdbcTemplate().query("select id from sec_user where login = ?",
				new Object[]{login},
				new int[]{Types.CHAR},
				new RowMapper<Integer>() {

					@Override
					public Integer mapRow(ResultSet rs, int arg1)
							throws SQLException {
						return rs.getInt("id");
					}
				});
		 return list.size()!=0 ? list.get(0).intValue() : 0;
	}

	
}
