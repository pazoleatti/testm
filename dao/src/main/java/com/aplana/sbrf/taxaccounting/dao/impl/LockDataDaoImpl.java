package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.LockDataDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.SecuredEntity;
import com.aplana.sbrf.taxaccounting.model.exception.LockException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalDateTime;
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
    private static final String LOCK_DATA_DELETE_ERROR = "Ошибка при удалении блокировок. %s";
    private static final String USER_LOCK_DATA_DELETE_ERROR = "Ошибка при удалении блокировок для пользователя с id = %d. %s";

    @Override
    public LockData get(String key, boolean like) {
        try {
            String sql = "SELECT id, key, user_id, task_id, date_lock, description FROM lock_data WHERE key " + (like ? "LIKE ?" : "= ?");
            return getJdbcTemplate().queryForObject(sql,
                    new Object[]{like ? "%" + key + "%" : key},
                    new int[]{Types.VARCHAR},
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
    public LockData get(String key, LocalDateTime lockDate) {
        try {
            return getJdbcTemplate().queryForObject(
                    "SELECT id, key, user_id, task_id, date_lock, description FROM lock_data WHERE key = ? and date_lock = ?",
                    new Object[]{key, lockDate.toDate()},
                    new int[]{Types.VARCHAR, Types.TIMESTAMP},
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
    public void lock(String key, int userId, String description) {
        try {
            Date lockDate = LocalDateTime.now().toDate();
            getJdbcTemplate().update("INSERT INTO lock_data (id, key, user_id, date_lock, description) VALUES (seq_lock_data.nextval, ?, ?, ?, ?)",
                    new Object[]{key,
                            userId,
                            lockDate,
                            description
                    },
                    new int[]{Types.VARCHAR, Types.NUMERIC, Types.TIMESTAMP, Types.VARCHAR});
        } catch (DataAccessException e) {
            throw new LockException("Ошибка при создании блокировки (%s, %s). %s", key, userId, e.getMessage());
        }
    }

    @Override
    public void lock(String key, int userId) {
        try {
            Date lockDate = LocalDateTime.now().toDate();
            getJdbcTemplate().update("INSERT INTO lock_data (id, key, user_id, date_lock) VALUES (seq_lock_data.nextval, ?, ?, ?)",
                    new Object[]{key,
                            userId,
                            lockDate
                    },
                    new int[]{Types.VARCHAR, Types.NUMERIC, Types.TIMESTAMP});
        } catch (DataAccessException e) {
            throw new LockException("Ошибка при создании блокировки (%s, %s). %s", key, userId, e.getMessage());
        }
    }

    @Override
    public void unlock(String key) {
        try {
            int affectedCount = getJdbcTemplate().update("DELETE FROM lock_data WHERE key = ?",
                    new Object[]{key},
                    new int[]{Types.VARCHAR});
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
            getJdbcTemplate().update("DELETE FROM lock_data ld WHERE user_id = ?", userId);
        } catch (Exception e) {
            LOG.error(String.format(USER_LOCK_DATA_DELETE_ERROR, userId, e.getMessage()), e);
            if (!ignoreError) {
                throw new LockException(USER_LOCK_DATA_DELETE_ERROR, userId, e.getMessage());
            }
        }
    }

    @Override
    public void unlockAllByTask(long taskId) {
        getJdbcTemplate().update("delete from lock_data where task_id = ?", taskId);
    }

    @Override
    public PagingResult<LockData> getLocks(String filter, PagingParams pagingParams) {
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("start", pagingParams.getStartIndex() + 1);
            params.put("count", pagingParams.getStartIndex() + pagingParams.getCount());
            if (!StringUtils.isEmpty(filter)) {
                params.put("filter", "%" + filter.toLowerCase() + "%");
            }
            String sql = " (SELECT ld.id, ld.key, ld.user_id, ld.task_id, ld.date_lock, ld.description, u.login, \n" +
                    (isSupportOver() ? "ROW_NUMBER() OVER (ORDER BY date_lock)" : "ROWNUM") +
                    " AS rn \n" +
                    "FROM lock_data ld \n"
                    + "join sec_user u on u.id = ld.user_id \n" +
                    "WHERE ld.task_id is null" +
                    (!StringUtils.isEmpty(filter) ?
                            " AND (LOWER(ld.key) LIKE :filter OR LOWER(ld.description) LIKE :filter OR LOWER(u.login) LIKE :filter OR LOWER(u.name) LIKE :filter) "
                            : "")
                    + ") \n";
            if (LOG.isTraceEnabled()) {
                LOG.trace(params);
                LOG.trace(sql);
            }
            String fullSql = "SELECT * FROM" + sql + "WHERE rn BETWEEN :start AND :count";
            String countSql = "SELECT COUNT(*) FROM" + sql;
            List<LockData> records = getNamedParameterJdbcTemplate().query(fullSql, params, new LockDataMapper());
            int count = getNamedParameterJdbcTemplate().queryForObject(countSql, params, Integer.class);
            return new PagingResult<LockData>(records, count);
        } catch (EmptyResultDataAccessException e) {
            // недостижимое место из-за особенности запроса
            return new PagingResult<LockData>(new ArrayList<LockData>(), 0);
        }
    }

    @Override
    public void unlockAll(List<Long> ids) {
        getJdbcTemplate().update("DELETE FROM lock_data ld WHERE " + SqlUtils.transformToSqlInStatement("id", ids));
    }

    @Override
    public SecuredEntity getSecuredEntity(long id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private static final class LockDataMapper implements RowMapper<LockData> {
        @Override
        public LockData mapRow(ResultSet rs, int index) throws SQLException {
            LockData result = new LockData();
            result.setId(rs.getLong("id"));
            result.setKey(rs.getString("key"));
            result.setUserId(rs.getInt("user_id"));
            result.setTaskId(SqlUtils.getLong(rs, "task_id"));
            result.setDateLock(new LocalDateTime(rs.getTimestamp("date_lock")));
            result.setDescription(rs.getString("description"));
            return result;
        }
    }

    @Override
    public List<String> getLockIfOlderThan(long seconds) {
        Date now = LocalDateTime.now().toDate();
        try {
            return getJdbcTemplate().queryForList(
                    "SELECT key FROM lock_data WHERE date_lock < (? - INTERVAL '" + seconds + "' SECOND)",
                    new Object[]{now},
                    new int[]{Types.TIMESTAMP},
                    String.class);
        } catch (DataAccessException e) {
            LOG.error(String.format(LOCK_DATA_DELETE_ERROR, e.getMessage()), e);
            throw new LockException(LOCK_DATA_DELETE_ERROR, e.getMessage());
        }
    }

    @Override
    public void bindTask(String lockKey, long taskId) {
        getJdbcTemplate().update("update lock_data set task_id = ? where key = ?", taskId, lockKey);
    }

}
