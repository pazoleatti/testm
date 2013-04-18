package com.aplana.sbrf.taxaccounting.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cache.annotation.Cacheable;
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
	
	private static final class TARoleMapper implements RowMapper<TARole> {
		public TARole mapRow(ResultSet rs, int index) throws SQLException {
			TARole result = new TARole();
			result.setId(rs.getInt("id"));
			result.setName(rs.getString("name"));
			result.setAlias(rs.getString("alias"));
			return result;
		}
	}
	
	private static final class TAUserMapper implements RowMapper<TAUser> {
		public TAUser mapRow(ResultSet rs, int index) throws SQLException {
			TAUser result = new TAUser();
			result.setId(rs.getInt("id"));
			result.setName(rs.getString("name"));
			result.setActive(rs.getBoolean("is_active"));
			result.setDepartmentId(rs.getInt("department_id"));
			result.setLogin(rs.getString("login"));
			result.setEmail(rs.getString("email"));
			result.setUuid(rs.getString("uuid"));
			return result;
		}
	}
	
	@Override
	public TAUser getUser(String login) {
		TAUser user;
		if (logger.isDebugEnabled()) {
			logger.debug("Fetching TAUser with login = " + login);	
		}
		try {
			user = getJdbcTemplate().queryForObject(
				"select * from sec_user where login = ?",
				new Object[] { login },
				new int[] { Types.CHAR },
				new TAUserMapper()
			);
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException("Пользователь с логином \"" + login + "\" не найден в БД");
		}
		initUser(user);
		return user;
	}

	@Override
	@Cacheable("User")
	public TAUser getUser(int userId) {
		TAUser user;
		try {
			user = getJdbcTemplate().queryForObject(
				"select * from sec_user where id = ?",
				new Object[] { userId },
				new int[] { Types.NUMERIC },
				new TAUserMapper()
			);
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException("Пользователь с id = " + userId + " не найден в БД");
		}

		initUser(user);
		return user;
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
				new TARoleMapper()
			);
			user.setRoles(userRoles);
		}
	}
	
	@Override
	public List<TARole> listRolesAll() {
		return getJdbcTemplate().query(
				"select * from sec_role",
				new Object[] {},
				new int[] {},
				new TARoleMapper()
			);
	}

	@Transactional(readOnly=false)
	@Override
	public void addUser(final TAUser user) {
		final KeyHolder keyHolder = new GeneratedKeyHolder();
		try {
			PreparedStatementCreator psc = new PreparedStatementCreator() {
				
				@Override
				public PreparedStatement createPreparedStatement(Connection con)
						throws SQLException {
					
					//prepareStatement(sql, string_field) Only for ORACLE using
					PreparedStatement ps = con
							.prepareStatement(
									"insert into sec_user (id, name, login, department_id, is_active, email, uuid) values (seq_sec_user.nextval,?,?,?,?,?,?)",
									new String[]{"ID"});
					ps.setString(1, user.getName());
					ps.setString(2, user.getLogin());
					ps.setInt(3, user.getDepartmentId());
					ps.setBoolean(4, user.isActive());
					ps.setString(5, user.getEmail());
					ps.setString(6, user.getUuid());

					return ps;
				}
			}; 
			getJdbcTemplate().update(psc, keyHolder);
			getJdbcTemplate().batchUpdate("insert into sec_user_role (user_id, role_id) values (?, ?)",new BatchPreparedStatementSetter() {
				
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					TARole role = user.getRoles().get(i);
					ps.setInt(1, keyHolder.getKey().intValue());
					ps.setInt(2, role.getId());
				}
				
				@Override
				public int getBatchSize() {
					return user.getRoles().size();
				}
			});
		} catch (Exception e) {
			throw new DaoException("Пользователья с uuid = " + user.getUuid() + " не удалось сохранить.");
		}
		
	}

	@Transactional(readOnly=false)
	@Override
	public void setUserIsActive(TAUser user) {
		getJdbcTemplate().update("update sec_user set is_active=? where login = ?", 
				new Object[]{user.isActive(), user.getLogin()},
				new int[]{Types.BOOLEAN, Types.CHAR});
	}

	@Transactional(readOnly=false)
	@Override
	public void updateUserRoles(final TAUser user) {
		final int userRolesCount = getJdbcTemplate().queryForInt(
				"select count(*) from sec_user_role ur where ur.user_id = " +
				"(select id from sec_user where login=?)", user.getLogin()
			);
		getJdbcTemplate().batchUpdate("delete from sec_user_role where user_id=" +
				"(select id from sec_user where login=?)",new BatchPreparedStatementSetter() {
			
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				ps.setString(1, user.getLogin());
			}
			
			@Override
			public int getBatchSize() {
				return userRolesCount;
			}
		});
		getJdbcTemplate().batchUpdate("insert into sec_user_role (user_id, role_id) values (?, ?)",new BatchPreparedStatementSetter() {
			
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				TARole role = user.getRoles().get(i);
				ps.setInt(1, user.getId());
				ps.setInt(2, role.getId());
			}
			
			@Override
			public int getBatchSize() {
				return user.getRoles().size();
			}
		});
	}
	
}
