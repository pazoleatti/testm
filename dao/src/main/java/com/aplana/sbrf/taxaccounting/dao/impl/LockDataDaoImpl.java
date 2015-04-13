package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.LockDataDao;
import com.aplana.sbrf.taxaccounting.model.exception.LockException;
import com.aplana.sbrf.taxaccounting.model.LockData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Реализация дао блокировок
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 17.07.14 15:01
 */
@Repository
public class LockDataDaoImpl extends AbstractDao implements LockDataDao {

	private static final Log LOG = LogFactory.getLog(LockDataDaoImpl.class);

	@Override
	public LockData get(String key) {
		try {
            return getJdbcTemplate().queryForObject(
					"SELECT key, user_id, date_before, date_lock FROM lock_data WHERE key = ?",
					new Object[] {key},
					new int[] {Types.VARCHAR},
					new LockDataMapper()
			);
		} catch (EmptyResultDataAccessException e) {
			return null;
		} catch (Exception e) {
			throw new LockException("Ошибка при поиске блокировки с кодом = %s", key);
		}
	}

    @Override
    public LockData get(String key, Date dateBefore) {
        try {
            return getJdbcTemplate().queryForObject(
                    "SELECT key, user_id, date_before, date_lock FROM lock_data WHERE key = ? and date_before = ?",
                    new Object[] {key, dateBefore},
                    new int[] {Types.VARCHAR, Types.TIMESTAMP},
                    new LockDataMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (Exception e) {
            throw new LockException("Ошибка при поиске блокировки с кодом = %s и сроком жизни блокировки = %s", key, dateBefore);
        }
    }

    @Override
	public void createLock(String key, int userId, Date dateBefore) {
		try {
            getJdbcTemplate().update("INSERT INTO lock_data (key, user_id, date_before) VALUES (?,?,?)",
					new Object[] {key,
							userId,
							dateBefore},
					new int[] {Types.VARCHAR, Types.NUMERIC, Types.TIMESTAMP});
		} catch (DataAccessException e) {
			throw new LockException("Ошибка при создании блокировки (%s, %s, %s). %s", key, userId, dateBefore, e.getMessage());
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
				throw new LockException("Ошибка обновления. Блокировка с кодом = %s не найдена в БД.", key);
			}
		} catch (DataAccessException e) {
			throw new LockException("Ошибка при обновлении блокировки с кодом = %s. %s", key, e.getMessage());
		}
	}

	@Override
	public void deleteLock(String key) {
		try {
			int affectedCount = getJdbcTemplate().update("DELETE FROM lock_data WHERE key = ?",
					new Object[] {key},
					new int[] {Types.VARCHAR});
			if (affectedCount == 0) {
				throw new LockException("Ошибка удаления. Блокировка с кодом = %s не найдена в БД.", key);
			}
		} catch (DataAccessException e) {
			throw new LockException("Ошибка при удалении блокировки с кодом = %s. %s", key, e.getMessage());
		}
	}

    @Override
    public void unlockAllByUserId(int userId, boolean ignoreError) {
        try {
            getJdbcTemplate().update("delete from lock_data ld where user_id = ? and (not exists (select 1 from lock_data_subscribers lds where lds.lock_key=ld.key) or ld.date_before < sysdate)", userId);
        } catch (DataAccessException e) {
            if (ignoreError) {
				LOG.error(e);
                return;
            }
            throw new LockException("Ошибка при удалении блокировок для пользователя с id = %d. %s", userId, e.getMessage());
        } catch (Exception e) {
            if (ignoreError) {
				LOG.error(e);
            }
        }
    }

    @Override
    public void unlockIfOlderThan(int sec) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, -sec);
        try {
            getJdbcTemplate().update(
                    "delete from lock_data where date_before < ?",
                    cal.getTime()
            );
        } catch (DataAccessException e){
            logger.error("", e);
            throw new LockException("Ошибка при удалении блокировок. %s", e.getMessage());
        }
    }

    @Override
    public List<Integer> getUsersWaitingForLock(String key) {
        try {
            return getJdbcTemplate().query(
                    "select user_id from lock_data_subscribers where lock_key = ?",
                    new Object[]{key},
                    new int[]{Types.VARCHAR},
                    new RowMapper<Integer>() {
                        @Override
                        public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                            return rs.getInt("user_id");
                        }
                    }
            );
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Integer>();
        }
    }

    @Override
    public void addUserWaitingForLock(String key, int userId) {
        try {
            getJdbcTemplate().update("INSERT INTO lock_data_subscribers (lock_key, user_id) VALUES (?,?)",
                    new Object[] {key, userId},
                    new int[] {Types.VARCHAR, Types.NUMERIC});
        } catch (DataAccessException e) {
            throw new LockException("Ошибка при добавлении пользователя в список ожидающих объект блокировки (%s, %s). %s", key, userId, e.getMessage());
        }

    }

    @Override
    @Cacheable(value = "PermanentData", key = "'LockTimeout_'+#lockObject.name()")
    public int getLockTimeout(LockData.LockObjects lockObject) {
        return getJdbcTemplate().queryForInt("select timeout from configuration_lock where key = ? ", lockObject.name());
    }

    private static final class LockDataMapper implements RowMapper<LockData> {
		@Override
        public LockData mapRow(ResultSet rs, int index) throws SQLException {
			LockData result = new LockData();
			result.setKey(rs.getString("key"));
			result.setUserId(rs.getInt("user_id"));
			result.setDateBefore(rs.getTimestamp("date_before"));
            result.setDateLock(rs.getTimestamp("date_lock"));
			return result;
		}
	}
}
