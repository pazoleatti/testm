package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.LockDataDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.util.DBUtils;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.LockDataDTO;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.SecuredEntity;
import com.aplana.sbrf.taxaccounting.model.exception.LockException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Реализация дао блокировок
 */
@Repository
public class LockDataDaoImpl extends AbstractDao implements LockDataDao {

    private static final Log LOG = LogFactory.getLog(LockDataDaoImpl.class);
    private static final String LOCK_DATA_DELETE_ERROR = "Ошибка при удалении блокировок. %s";
    private static final String USER_LOCK_DATA_DELETE_ERROR = "Ошибка при удалении блокировок для пользователя с id = %d. %s";

    @Autowired
    private DBUtils dbUtils;


    @Override
    public LockData findByKey(String key) {
        try {
            String sql = "SELECT id, key, user_id, task_id, date_lock, description FROM lock_data WHERE key = ?";
            return getJdbcTemplate().queryForObject(sql,
                    new Object[]{key},
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
    public boolean existsByKey(String key) {
        String query = "select count(id) from lock_data where key = ?";
        int count = getJdbcTemplate().queryForObject(query, new Object[]{key}, new int[]{Types.VARCHAR}, Integer.class);
        return (count > 0);
    }

    @Override
    public boolean existsByKeyAndUserId(String key, int userId) {
        String query = "select count(id) from lock_data where key = ? and user_id = ?";
        int count = getJdbcTemplate().queryForObject(query, new Object[]{key, userId}, new int[]{Types.VARCHAR, Types.INTEGER}, Integer.class);
        return (count > 0);
    }

    @Override
    public void lock(String key, int userId, String description) {
        try {
            Date lockDate = new Date();
            getJdbcTemplate().update("INSERT INTO lock_data (id, key, user_id, date_lock, description) VALUES (seq_lock_data.nextval, ?, ?, ?, ?)",
                    new Object[]{
                            key,
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
                    new Object[]{
                            key,
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
    public void unlockAllByTaskId(long taskId) {
        getJdbcTemplate().update("delete from lock_data where task_id = ?", taskId);
    }

    @Override
    public PagingResult<LockDataDTO> getLocks(String filter, PagingParams pagingParams) {
        Map<String, Object> params = new HashMap<>();
        params.put("start", pagingParams.getStartIndex() + 1);
        params.put("count", pagingParams.getStartIndex() + pagingParams.getCount());

        String whereClause = "";
        if (StringUtils.isNotEmpty(filter)) {
            params.put("filter", "%" + filter.toLowerCase() + "%");
            whereClause = " where (lower(ld.key) like :filter or lower(ld.description) like :filter or lower(u.login) like :filter or lower(u.name) like :filter) ";
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
    public SecuredEntity findSecuredEntityById(long id) {
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
    public List<LockData> fetchAllByKeySet(Collection<String> keysBlocker) {
        List<String> keys = new ArrayList(keysBlocker);
        if (keys.size() > IN_CLAUSE_LIMIT) {
            List<LockData> result = new ArrayList<>();
            int n = (keys.size() - 1) / IN_CLAUSE_LIMIT + 1;
            for (int i = 0; i < n; i++) {
                List<String> subList = keys.subList(i * IN_CLAUSE_LIMIT, Math.min((i + 1) * IN_CLAUSE_LIMIT, keys.size()));
                List<LockData> subResult = fetchAllByKeySet(subList);
                result.addAll(subResult);
            }
            return result;
        }
        if (keysBlocker.size() == 0) {
            return new ArrayList<>();
        }
        try {
            return getJdbcTemplate().query(
                    "select id, key, user_id, task_id, date_lock, description from lock_data " +
                            " where " + SqlUtils.transformToSqlInStatementForString("key", keysBlocker),
                    new LockDataMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public void lockKeysBatch(Map<String, String> lockKeysWithDescription, int userId) {
        List<Long> ids = dbUtils.getNextIds("seq_lock_data", lockKeysWithDescription.size());
        Date dateLock = new Date();
        String sql = "insert into lock_data (id, key, user_id, date_lock, description) " +
                "values (:id, :key, :userId, :dateLock, :description)";
        List<Map<String, Object>> batchValues = new ArrayList<>(lockKeysWithDescription.size());

        int index = 0;
        for (Map.Entry<String, String> entry : lockKeysWithDescription.entrySet()) {
            batchValues.add(new MapSqlParameterSource("id", ids.get(index))
                    .addValue("key", entry.getKey())
                    .addValue("userId", userId)
                    .addValue("dateLock", dateLock)
                    .addValue("description", entry.getValue())
                    .getValues());
            index++;
        }
        getNamedParameterJdbcTemplate().batchUpdate(sql, batchValues.toArray(new Map[lockKeysWithDescription.size()]));
    }

    @Override
    public void bindTaskToMultiKeys(Collection<String> keys, long taskId) {
        String sql = "update lock_data set task_id = :taskId where key = :key";
        List<Map<String, Object>> batchValues = new ArrayList<>(keys.size());
        for (String key : keys) {
            batchValues.add(new MapSqlParameterSource("taskId", taskId)
                    .addValue("key", key)
                    .getValues());
        }
        getNamedParameterJdbcTemplate().batchUpdate(sql, batchValues.toArray(new Map[keys.size()]));
    }

    @Override
    public void unlockMultipleTasks(Collection<String> keys) {
        if (keys != null) {
            String sql = "delete from lock_data where key = :key";
            List<Map<String, Object>> batchValues = new ArrayList<>(keys.size());
            for (String key : keys) {
                batchValues.add(new MapSqlParameterSource("key", key).getValues());
            }
            getNamedParameterJdbcTemplate().batchUpdate(sql, batchValues.toArray(new Map[keys.size()]));
        }
    }
}
