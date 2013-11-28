package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.TARoleDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.TARole;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

@Repository
@Transactional(readOnly=true)
public class TARoleDaoImpl extends AbstractDao implements TARoleDao {

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

	@Override
	public TARole getRole(Integer id) {
		TARole role;
		try {
			role = getJdbcTemplate().queryForObject(
					"select * from sec_role where id = ?",
					new Object[] { id },
					new int[] { Types.NUMERIC },
					TA_ROLE_MAPPER
			);
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException("Роль с id = " + id + " не найден в БД");
		}

		return role;
	}

	@Override
	public List<Integer> getAll() {
		try {
			return getJdbcTemplate().queryForList("select id from sec_role", Integer.class);
		} catch (DataAccessException e) {
			throw new DaoException("Ошибка при получении ролей. " + e.getLocalizedMessage());
		}
	}
}
