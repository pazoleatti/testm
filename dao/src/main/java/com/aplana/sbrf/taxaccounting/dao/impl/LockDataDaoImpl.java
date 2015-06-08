package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.model.BalancingVariants;
import com.aplana.sbrf.taxaccounting.dao.LockDataDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
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
import java.util.*;

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
	public LockData get(String key, boolean like) {
		try {
            String fullKey = like ? "%" + key + "%" : key;
            String sql = "SELECT key, user_id, date_before, date_lock, description, state, state_date, queue, queue_position FROM lock_data \n " +
                            "join (select q_key, queue_position from (select ld.key as q_key, " +
                    (isSupportOver() ? "row_number() over (partition by ld.queue order by ld.date_lock)" : "rownum") + " as queue_position from lock_data ld)) q on q.q_key = key \n" +
                            "WHERE key " + (like ? "like ?" : "= ?");
            return getJdbcTemplate().queryForObject(sql,
					new Object[] {fullKey},
					new int[] {Types.VARCHAR},
					new LockDataMapper()
			);
		} catch (EmptyResultDataAccessException e) {
			return null;
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			throw new LockException("Ошибка при поиске блокировки с кодом = %s", key);
		}
	}

    @Override
    public LockData get(String key, Date lockDate) {
        try {
            return getJdbcTemplate().queryForObject(
                    "SELECT key, user_id, date_before, date_lock, description, state, state_date, queue, queue_position FROM lock_data \n" +
                            "join (select q_key, queue_position from (select ld.key as q_key, " +
                            (isSupportOver() ? "row_number() over (partition by ld.queue order by ld.date_lock)" : "rownum") + " as queue_position from lock_data ld)) q on q.q_key = key \n" +
                            "WHERE key = ? and date_lock = ?",
                    new Object[] {key, lockDate},
                    new int[] {Types.VARCHAR, Types.TIMESTAMP},
                    new LockDataMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (Exception e) {
			LOG.error(e.getMessage(), e);
            throw new LockException("Ошибка при поиске блокировки с кодом = %s и датой начала = %s", key, lockDate);
        }
    }

    @Override
	public void createLock(String key, int userId, Date dateBefore, String description, String state) {
		try {
            getJdbcTemplate().update("INSERT INTO lock_data (key, user_id, date_before, description, state, state_date) VALUES (?,?,?,?,?,sysdate)",
					new Object[] {key,
							userId,
							dateBefore,
                            description,
                            state
                    },
					new int[] {Types.VARCHAR, Types.NUMERIC, Types.TIMESTAMP, Types.VARCHAR, Types.VARCHAR});
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

    @Override
    public PagingResult<LockData> getLocks(String filter,  LockData.LockQueues queues, PagingParams pagingParams) {
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            String queueSql = "1 = 1";
            switch (queues) {
                case SHORT:
                    queueSql = "queue = :queue";
                    params.put("queue", BalancingVariants.SHORT.getId());
                    break;
                case LONG:
                    queueSql = "queue = :queue";
                    params.put("queue", BalancingVariants.LONG.getId());
                    break;
                case NONE:
                    queueSql = "queue = 0";
                    break;
            }
            params.put("start", pagingParams.getStartIndex() + 1);
            params.put("count", pagingParams.getStartIndex() + pagingParams.getCount());
            params.put("filter", "%" + filter.toLowerCase() + "%");
            String sql = " (SELECT ld.key, ld.user_id, ld.date_before, ld.date_lock, ld.state, ld.state_date, ld.description, ld.queue, u.login, \n" +
                    "row_number() over (partition by queue order by date_lock) as queue_position, row_number() over (order by queue, date_lock) as rn \n" +
                    "FROM lock_data ld \n"
                    + "join sec_user u on u.id = ld.user_id \n" +
                    "where " + queueSql + " \n"
                    + (filter != null && !filter.isEmpty() ?
                    "and lower(ld.key) like :filter or lower(ld.description) like :filter or lower(ld.state) like :filter or lower(u.login) like :filter or lower(u.name) like :filter "
                    : "")
                    + "order by queue, queue_position) \n";

            String fullSql = "select * from" + sql + "where rn between :start and :count";
            String countSql = "select count(*) from" + sql;
            List<LockData> records = getNamedParameterJdbcTemplate().query(fullSql, params, new LockDataMapper());
            int count = getNamedParameterJdbcTemplate().queryForInt(countSql, params);
            return new PagingResult<LockData>(records, count);
        } catch (EmptyResultDataAccessException e) {
            return new PagingResult<LockData>(new ArrayList<LockData>(), 0);
        }
    }

    @Override
    public void unlockAll(List<String> keys) {
        getJdbcTemplate().update("delete from lock_data ld where " + SqlUtils.transformToSqlInStatementForString("key", keys));
    }

    @Override
    public void extendAll(List<String> keys, int hours) {
        getJdbcTemplate().update("update lock_data set date_before = date_before + interval '"+ hours +
                "' hour where " + SqlUtils.transformToSqlInStatementForString("key", keys));

    }

    @Override
    public void updateState(String key, Date lockDate, String state) {
        getJdbcTemplate().update("update lock_data set state = ?, state_date = sysdate where key = ? and date_lock = ?",
                new Object[] {state, key, lockDate},
                new int[] {Types.VARCHAR, Types.VARCHAR, Types.TIMESTAMP});
    }

    @Override
    public void updateQueue(String key, Date lockDate, BalancingVariants queue) {
        getJdbcTemplate().update("update lock_data set queue = ?, state_date = sysdate where key = ? and date_lock = ?",
                new Object[] {queue.getId(), key, lockDate},
                new int[] {Types.INTEGER, Types.VARCHAR, Types.TIMESTAMP});
    }

    private static final class LockDataMapper implements RowMapper<LockData> {
		@Override
        public LockData mapRow(ResultSet rs, int index) throws SQLException {
			LockData result = new LockData();
			result.setKey(rs.getString("key"));
			result.setUserId(rs.getInt("user_id"));
			result.setDateBefore(rs.getTimestamp("date_before"));
            result.setDateLock(rs.getTimestamp("date_lock"));
            result.setState(rs.getString("state"));
            result.setStateDate(result.getState() != null ? rs.getTimestamp("state_date") : null);
            result.setDescription(rs.getString("description"));
            result.setQueue(LockData.LockQueues.getById(rs.getInt("queue")));
            result.setQueuePosition(rs.getInt("queue_position"));
			return result;
		}
	}
}
