package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.LockDataDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.BalancingVariants;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.exception.LockException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Реализация дао блокировок
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 17.07.14 15:01
 */
@Repository
public class LockDataDaoImpl extends AbstractDao implements LockDataDao {

    private static final Log LOG = LogFactory.getLog(LockDataDaoImpl.class);
	private static final String LOCK_DATA_DELETE_ERROR = "Ошибка при удалении блокировок. %s";
	private static final String USER_LOCK_DATA_DELETE_ERROR = "Ошибка при удалении блокировок для пользователя с id = %d. %s";

    @Override
    public LockData get(String key, boolean like) {
        try {
            String sql = "SELECT key, user_id, date_lock, description, state, state_date, queue, queue_position, server_node " +
                    "FROM lock_data \n " +
                    "JOIN (SELECT q_key, queue_position FROM (SELECT ld.key AS q_key, " +
                    (isSupportOver() ? "ROW_NUMBER() OVER (PARTITION BY ld.queue ORDER BY ld.date_lock)" : "rownum") + " AS queue_position FROM lock_data ld)) q ON q.q_key = key \n" +
                    "WHERE key " + (like ? "LIKE ?" : "= ?");
            return getJdbcTemplate().queryForObject(sql,
                    new Object[] {like ? "%" + key + "%" : key},
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
                    "SELECT key, user_id, date_lock, description, state, state_date, queue, queue_position, server_node " +
                            "FROM lock_data \n" +
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
    public void lock(String key, int userId, String description, String state, String serverNode) {
        try {
            getJdbcTemplate().update("INSERT INTO lock_data (key, user_id, description, state, state_date, server_node) VALUES (?, ?, ?, ?, sysdate, ?)",
                    new Object[] {key,
                            userId,
                            description,
                            state,
                            serverNode
                    },
                    new int[] {Types.VARCHAR, Types.NUMERIC, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR});
        } catch (DataAccessException e) {
            throw new LockException("Ошибка при создании блокировки (%s, %s). %s", key, userId, e.getMessage());
        }
    }

    @Override
    public void unlock(String key) {
        try {
            int affectedCount = getJdbcTemplate().update("DELETE FROM lock_data WHERE key = ?",
                    new Object[] {key},
                    new int[] {Types.VARCHAR});
            if (affectedCount == 0) {
                throw new LockException("Ошибка удаления. Блокировка с кодом \"%s\" не найдена в БД.", key);
            }
        } catch (DataAccessException e) {
			LOG.error(String.format(LOCK_DATA_DELETE_ERROR, e.getMessage()), e);
            throw new LockException("Ошибка при удалении блокировки с кодом \"%s\". %s", key, e.getMessage());
        }
    }

    @Override
    public void unlockAllByUserId(int userId, boolean ignoreError) {
        try {
            getJdbcTemplate().update("DELETE FROM lock_data ld WHERE user_id = ? AND (NOT EXISTS (SELECT 1 FROM lock_data_subscribers lds WHERE lds.lock_key=ld.key))", userId);
        } catch (Exception e) {
			LOG.error(String.format(USER_LOCK_DATA_DELETE_ERROR, userId, e.getMessage()), e);
            if (!ignoreError) {
				throw new LockException(USER_LOCK_DATA_DELETE_ERROR, userId, e.getMessage());
            }
        }
    }

    @Override
    public List<Integer> getUsersWaitingForLock(String key) {
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
    }

    @Override
    public void addUserWaitingForLock(String key, int userId) {
        try {
            getJdbcTemplate().update("INSERT INTO lock_data_subscribers (lock_key, user_id) VALUES (?, ?)",
                    new Object[] {key, userId},
                    new int[] {Types.VARCHAR, Types.NUMERIC});
        } catch (DataAccessException e) {
			LOG.error("Ошибка при добавлении пользователя в список ожидающих объект блокировки", e);
            throw new LockException("Ошибка при добавлении пользователя в список ожидающих объект блокировки (%s, %s). %s", key, userId, e.getMessage());
        }
    }

    @Override
    public PagingResult<LockData> getLocks(String filter, LockData.LockQueues queues, PagingParams pagingParams) {
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            String queueSql = "1 = 1";
			if (queues != null) {
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
					default:
				}
			}
            params.put("start", pagingParams.getStartIndex() + 1);
            params.put("count", pagingParams.getStartIndex() + pagingParams.getCount());
			String filterParam = filter == null ? "" : filter;
            params.put("filter", "%" + filterParam.toLowerCase() + "%");
            String sql = " (SELECT ld.key, ld.user_id, ld.date_lock, ld.state, ld.state_date, ld.description, ld.queue, ld.server_node, u.login, \n" +
					(isSupportOver() ? "ROW_NUMBER() OVER (partition BY queue ORDER BY date_lock)" : "ROWNUM") +
                    " AS queue_position, " +
					(isSupportOver() ? "ROW_NUMBER() OVER (ORDER BY queue, date_lock)" : "ROWNUM") +
					" AS rn \n" +
                    "FROM lock_data ld \n"
                    + "join sec_user u on u.id = ld.user_id \n" +
                    "WHERE " + queueSql + " \n"
                    + (!filterParam.isEmpty() ?
                    "AND (LOWER(ld.key) LIKE :filter OR LOWER(ld.description) LIKE :filter OR LOWER(ld.state) LIKE :filter OR LOWER(u.login) LIKE :filter OR LOWER(u.name) LIKE :filter OR LOWER(ld.server_node) LIKE :filter) "
                    : "")
                    + "ORDER BY queue DESC, queue_position) \n";
			if (LOG.isTraceEnabled()) {
				LOG.trace(params);
				LOG.trace(sql.toString());
			}
            String fullSql = "SELECT * FROM" + sql + "WHERE rn BETWEEN :start AND :count";
            String countSql = "SELECT COUNT(*) FROM" + sql;
            List<LockData> records = getNamedParameterJdbcTemplate().query(fullSql, params, new LockDataMapper());
            int count = getNamedParameterJdbcTemplate().queryForInt(countSql, params);
            return new PagingResult<LockData>(records, count);
        } catch (EmptyResultDataAccessException e) {
			// недостижимое место из-за особенности запроса
            return new PagingResult<LockData>(new ArrayList<LockData>(), 0);
        }
    }

    @Override
    public void unlockAll(List<String> keys) {
        getJdbcTemplate().update("DELETE FROM lock_data ld WHERE " + SqlUtils.transformToSqlInStatementForString("key", keys));
    }

    @Override
    public void updateState(String key, Date lockDate, String state, String serverNode) {
        getJdbcTemplate().update("UPDATE lock_data SET state = ?, state_date = sysdate, server_node = ? WHERE KEY = ? AND date_lock = ?",
                new Object[] {state, serverNode, key, lockDate},
                new int[] {Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.TIMESTAMP});
    }

    @Override
    public void updateQueue(String key, Date lockDate, LockData.LockQueues queue) {
        getJdbcTemplate().update("UPDATE lock_data SET queue = ?, state_date = sysdate WHERE key = ? AND date_lock = ?",
                new Object[] {queue.getId(), key, lockDate},
                new int[] {Types.INTEGER, Types.VARCHAR, Types.TIMESTAMP});
    }

    private static final class LockDataMapper implements RowMapper<LockData> {
        @Override
        public LockData mapRow(ResultSet rs, int index) throws SQLException {
            LockData result = new LockData();
            result.setKey(rs.getString("key"));
            result.setUserId(rs.getInt("user_id"));
            result.setDateLock(rs.getTimestamp("date_lock"));
            result.setState(rs.getString("state"));
            result.setStateDate(result.getState() != null ? rs.getTimestamp("state_date") : null);
            result.setDescription(rs.getString("description"));
            result.setQueue(LockData.LockQueues.getById(rs.getInt("queue")));
            result.setQueuePosition(rs.getInt("queue_position"));
            result.setServerNode(rs.getString("server_node"));
            return result;
        }
    }

	@Override
	public int unlockIfOlderThan(long seconds) {
		try {
			return getJdbcTemplate().update(
					"DELETE FROM lock_data WHERE date_lock < (SYSDATE - INTERVAL '" + seconds + "' SECOND)");
		} catch (DataAccessException e){
			LOG.error(String.format(LOCK_DATA_DELETE_ERROR, e.getMessage()), e);
			throw new LockException(LOCK_DATA_DELETE_ERROR, e.getMessage());
		}
	}

}
