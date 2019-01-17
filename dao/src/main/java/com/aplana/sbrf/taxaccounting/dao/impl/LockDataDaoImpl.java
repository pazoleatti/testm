package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.LockDataDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.LockDataDTO;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.SecuredEntity;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.exception.LockException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    public LockData get(String key, Date lockDate) {
        try {
            return getJdbcTemplate().queryForObject(
                    "SELECT id, key, user_id, task_id, date_lock, description FROM lock_data WHERE key = ? and date_lock = ?",
                    new Object[]{key, lockDate},
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
            Date lockDate = new Date();
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
            Date lockDate = new Date();
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
        getJdbcTemplate().update("DELETE FROM lock_data WHERE key = ?",
                new Object[]{key},
                new int[]{Types.VARCHAR});
    }

    @Override
    public void unlockOld(String key) {
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
    public PagingResult<LockDataDTO> getLocks(String filter, PagingParams pagingParams, TAUser user) {
        Map<String, Object> params = new HashMap<>();
        params.put("start", pagingParams.getStartIndex() + 1);
        params.put("count", pagingParams.getStartIndex() + pagingParams.getCount());
        String whereClause = " where 1=1 ";
        if (!StringUtils.isEmpty(filter)) {
            params.put("filter", "%" + filter.toLowerCase() + "%");
            whereClause += " and (lower(ld.key) like :filter or lower(ld.description) like :filter or lower(u.login) like :filter or lower(u.name) like :filter) ";
        }
        if (!user.hasRoles(TARole.ROLE_ADMIN)) {
            params.put("userId", user.getId());
            whereClause += " and ld.user_id = :userId ";
        }
        String sql = "select ld.id, ld.key, u.name || ' (' || u.login || ')' as user_name, ld.date_lock, ld.description, " +
                (isSupportOver() ? "row_number() over (order by date_lock)" : "rownum") + " as rn\n" +
                "from lock_data ld\n" +
                "join sec_user u on u.id = ld.user_id\n" +
                whereClause;

        List<LockDataDTO> records = getNamedParameterJdbcTemplate().query(
                "SELECT * FROM (" + sql + ") WHERE rn BETWEEN :start AND :count",
                params, new RowMapper<LockDataDTO>() {
                    @Override
                    public LockDataDTO mapRow(ResultSet rs, int index) throws SQLException {
                        LockDataDTO result = new LockDataDTO();
                        result.setId(rs.getLong("id"));
                        result.setKey(rs.getString("key"));
                        result.setUser(rs.getString("user_name"));
                        result.setDateLock(rs.getTimestamp("date_lock"));
                        result.setDescription(rs.getString("description"));
                        return result;
                    }
                });
        int count = getNamedParameterJdbcTemplate().queryForObject("SELECT COUNT(*) FROM (" + sql + ")", params, Integer.class);
        return new PagingResult<>(records, count);
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
            result.setDateLock(rs.getTimestamp("date_lock"));
            result.setDescription(rs.getString("description"));
            return result;
        }
    }

    @Override
    public List<String> getLockIfOlderThan(long seconds) {
        try {
            return getJdbcTemplate().queryForList(
                    "SELECT key FROM lock_data WHERE date_lock < (SYSDATE - INTERVAL '" + seconds + "' SECOND)", String.class);
        } catch (DataAccessException e) {
            LOG.error(String.format(LOCK_DATA_DELETE_ERROR, e.getMessage()), e);
            throw new LockException(LOCK_DATA_DELETE_ERROR, e.getMessage());
        }
    }

    @Override
    public void bindTask(String lockKey, long taskId) {
        getJdbcTemplate().update("update lock_data set task_id = ? where key = ?", taskId, lockKey);
    }

    @Override
    public List<LockData> fetchAllByKeySet(Set<String> keysBlocker) {
        return getJdbcTemplate().query(
                "select id, key, user_id, task_id, date_lock, description from lock_data " +
                        " where " + SqlUtils.transformToSqlInStatementForString("key", keysBlocker),
                new LockDataMapper());
    }

}
