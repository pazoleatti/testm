package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.LockDataDao;
import com.aplana.sbrf.taxaccounting.model.exception.LockException;
import com.aplana.sbrf.taxaccounting.model.LockData;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;

/**
 * Реализация дао блокировок
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 17.07.14 15:01
 */
@Repository
public class LockDataDaoImpl extends AbstractDao implements LockDataDao {

	@Override
	public LockData get(String key) {
		try {
			return getJdbcTemplate().queryForObject(
					"SELECT key, user_id, date_before FROM lock_data WHERE key = ?",
					new Object[] {key},
					new int[] {Types.VARCHAR},
					new LockDataMapper()
			);
		} catch (EmptyResultDataAccessException e) {
			return null;
		} catch (Exception e) {
			throw new LockException("Ошибка при поиске блокировки с кодом = \"" + key + "\"");
		}
	}

	@Override
	public void createLock(String key, long userId, Date dateBefore) {
		try {
			getJdbcTemplate().update("INSERT INTO lock_data (key, user_id, date_before) VALUES (?,?,?)",
					new Object[] {key,
							userId,
							dateBefore},
					new int[] {Types.VARCHAR, Types.NUMERIC, Types.TIMESTAMP});
		} catch (DataAccessException e) {
			throw new LockException(String.format("Ошибка при создании блокировки. (%s, %s, %s)", key, userId, dateBefore), e.getMessage());
		}
	}

	@Override
	public void updateLock(String key, Date dateBefore) {
		try {
			int affectedCount = getJdbcTemplate().update("UPDATE lock_data SET date_before = ? WHERE key = ?",
					new Object[] {dateBefore,
							key},
					new int[] {Types.TIMESTAMP, Types.VARCHAR});
			if (affectedCount == 0) {
				throw new LockException("Ошибка обновления. Блокировка с кодом = \"" + key + "\" не найдена в БД");
			}
		} catch (DataAccessException e) {
			throw new LockException("Ошибка при обновлении блокировки с кодом = " + key + "\"", e.getMessage());
		}
	}

	@Override
	public void deleteLock(String key) {
		try {
			int affectedCount = getJdbcTemplate().update("DELETE FROM lock_data WHERE key = ?",
					new Object[] {key},
					new int[] {Types.VARCHAR});
			if (affectedCount == 0) {
				throw new LockException("Ошибка удаления. Блокировка с кодом = \"" + key + "\" не найдена в БД");
			}
		} catch (DataAccessException e) {
			throw new LockException("Ошибка при удалении блокировки с кодом = \"" + key + "\"", e.getMessage());
		}
	}

	private static final class LockDataMapper implements RowMapper<LockData> {
		public LockData mapRow(ResultSet rs, int index) throws SQLException {
			LockData result = new LockData();
			result.setKey(rs.getString("key"));
			result.setUserId(rs.getInt("user_id"));
			result.setDateBefore(rs.getTimestamp("date_before"));
			return result;
		}
	}
}
