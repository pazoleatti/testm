package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.AsyncTaskDao;
import com.aplana.sbrf.taxaccounting.dao.util.DBUtils;
import com.aplana.sbrf.taxaccounting.model.AsyncQueue;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskDTO;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskData;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskGroup;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskState;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskType;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.substring;

/**
 * Реализация {@link AsyncTaskDao} для работы с асинхронными задачами
 *
 * @author dloshkarev
 */
@Repository
public class AsyncTaskDaoImpl extends AbstractDao implements AsyncTaskDao {

    private static final Log LOG = LogFactory.getLog(AsyncTaskDaoImpl.class);

    private DBUtils dbUtils;

    public AsyncTaskDaoImpl(DBUtils dbUtils) {
        this.dbUtils = dbUtils;
    }

    private static class AsyncTaskDataMapperLight implements RowMapper<AsyncTaskData> {
        @Override
        public AsyncTaskData mapRow(ResultSet rs, int rowNum) throws SQLException {
            AsyncTaskData result = new AsyncTaskData();
            result.setId(rs.getLong("id"));
            result.setType(AsyncTaskType.getByAsyncTaskTypeId(rs.getInt("type_id")));
            result.setUserId(rs.getInt("user_id"));
            result.setState(AsyncTaskState.getById(rs.getInt("state")));
            result.setDescription(rs.getString("description"));
            result.setCreateDate(rs.getTimestamp("create_date"));
            result.setNode(rs.getString("node"));
            return result;
        }
    }

    private static class AsyncTaskDataMapper extends AsyncTaskDataMapperLight {
        @Override
        public AsyncTaskData mapRow(ResultSet rs, int rowNum) throws SQLException {
            AsyncTaskData result = super.mapRow(rs, rowNum);
            ObjectInputStream ois = null;
            try {
                Blob blob = rs.getBlob("serialized_params");
                if (!rs.wasNull()) {
                    ois = new ObjectInputStream(blob.getBinaryStream());
                    result.setParams((Map<String, Object>) ois.readObject());
                }
            } catch (Exception e) {
                throw new DaoException("Cannot deserialize async task params", e);
            } finally {
                if (ois != null) {
                    try {
                        ois.close();
                    } catch (IOException ignored) {
                    }
                }
            }
            return result;
        }
    }

    private static final class AsyncTaskDTOMapper implements RowMapper<AsyncTaskDTO> {

        @Override
        public AsyncTaskDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
            AsyncTaskDTO result = new AsyncTaskDTO();
            result.setId(rs.getLong("id"));
            result.setUser(rs.getString("user_name") + " (" + rs.getString("user_login") + ")");
            result.setCreateDate(rs.getTimestamp("create_date"));
            result.setNode(rs.getString("node"));
            result.setState(AsyncTaskState.getById(rs.getInt("state")).getText());
            result.setStateDate(rs.getTimestamp("state_date"));
            result.setDescription(rs.getString("description"));
            result.setQueue(AsyncQueue.getById(rs.getInt("queue")).getName());
            result.setQueuePosition(rs.getInt("queue_position"));
            return result;
        }
    }

    @Override
    public List<AsyncTaskData> findAll() {
        return getNamedParameterJdbcTemplate().query("select * from async_task", new AsyncTaskDataMapper());
    }

    @Override
    public PagingResult<AsyncTaskDTO> findAll(String filter, PagingParams pagingParams) {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("start", pagingParams.getStartIndex() + 1);
            params.addValue("count", pagingParams.getStartIndex() + pagingParams.getCount());
            String filterParam = filter == null ? "" : filter;
            params.addValue("filter", "%" + filterParam.toLowerCase() + "%");
            String sql = " (SELECT t.id, t.create_date, t.state, t.state_date, t.description, t.queue, t.node, u.name as user_name, u.login as user_login, \n" +
                    (isSupportOver() ? "ROW_NUMBER() OVER (partition BY queue ORDER BY create_date)" : "ROWNUM") +
                    " AS queue_position, " +
                    (isSupportOver() ? "ROW_NUMBER() OVER (ORDER BY queue, create_date)" : "ROWNUM") +
                    " AS rn \n" +
                    "FROM async_task t \n"
                    + "join sec_user u on u.id = t.user_id \n" +
                    (!filterParam.isEmpty() ?
                            "WHERE (LOWER(t.id) LIKE :filter OR LOWER(t.description) LIKE :filter OR LOWER(u.login) LIKE :filter OR LOWER(u.name) LIKE :filter OR LOWER(t.node) LIKE :filter) "
                            : "")
                    + ") \n";
            if (LOG.isTraceEnabled()) {
                LOG.trace(params);
                LOG.trace(sql);
            }
            String fullSql = "SELECT * FROM" + sql + "WHERE rn BETWEEN :start AND :count";
            String countSql = "SELECT COUNT(*) FROM" + sql;
            List<AsyncTaskDTO> records = getNamedParameterJdbcTemplate().query(fullSql, params, new AsyncTaskDTOMapper());
            int count = getNamedParameterJdbcTemplate().queryForObject(countSql, params, Integer.class);
            return new PagingResult<>(records, count);
        } catch (EmptyResultDataAccessException e) {
            // недостижимое место из-за особенности запроса
            return new PagingResult<>(new ArrayList<AsyncTaskDTO>(), 0);
        }
    }

    @Override
    public AsyncTaskData create(long taskTypeId, int userId, String description, AsyncQueue queue, String priorityNode,
                                AsyncTaskGroup taskGroup, Map<String, Object> params) {
        byte[] serializedParams;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(params);
            oos.flush();
            oos.close();
            bos.close();

            serializedParams = bos.toByteArray();
        } catch (Exception e) {
            throw new DaoException("Cannot serialize async task params", e);
        }

        long id = dbUtils.getNextIds(DBUtils.Sequence.ASYNC_TASK, 1).get(0);

        getJdbcTemplate().update("INSERT INTO async_task (id, type_id, user_id, description, queue, priority_node, task_group, serialized_params) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                id, taskTypeId, userId, substring(description, 0, 400), queue.getId(), priorityNode, taskGroup != null ? taskGroup.getId() : null, serializedParams);
        return findById(id);
    }

    @Override
    public Long findTaskIdToReserve(String node, String priorityNode, int timeoutHours, AsyncQueue queue, int maxTasksPerNode, boolean serialMode) {
        String serialModeClause = "";
        if (serialMode) {
            serialModeClause = "and \n(task_group is null or " +
                    "(task_group is not null and task_group not in (select task_group from async_task where start_process_date is not null and task_group is not null))) \n";
        }
        String sql = "select id from async_task where id = (select id from (\n" +
                " select id from async_task where ((:priorityNode is null) or (:priorityNode is not null and priority_node = :priorityNode)) and \n" +
                "queue = :queue and " +
                "((node is null or current_timestamp > start_process_date + interval '" + timeoutHours + "' hour) and :maxTasksPerNode > (select count(*) FROM async_task WHERE node = :node AND queue = :queue)) " +
                serialModeClause +
                "order by create_date\n" +
                ") where rownum = 1)";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("node", node)
                .addValue("priorityNode", priorityNode)
                .addValue("maxTasksPerNode", maxTasksPerNode)
                .addValue("queue", queue.getId());
        try {
            return getNamedParameterJdbcTemplate().queryForObject(sql, params, Long.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public boolean reserveTask(final String node, long taskId) {
        try {
            String select = "select id from async_task where id = :taskId for update skip locked";
            String update = "UPDATE async_task SET node = :node, state_date = current_timestamp, start_process_date = current_timestamp " +
                    "WHERE id = :lockedTask";
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("taskId", taskId)
                    .addValue("node", node);

            Long lockedTask = getNamedParameterJdbcTemplate().queryForObject(select, params, Long.class);
            params.addValue("lockedTask", lockedTask);

            return getNamedParameterJdbcTemplate().update(update, params) == 1;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    @Override
    public AsyncTaskData findById(long id) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);
        try {
            return getNamedParameterJdbcTemplate().queryForObject("SELECT * FROM async_task WHERE id = :id", params, new AsyncTaskDataMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public AsyncTaskData findByIdLight(long id) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);
        try {
            return getNamedParameterJdbcTemplate().queryForObject("SELECT * FROM async_task WHERE id = :id", params, new AsyncTaskDataMapperLight());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void updateState(long id, AsyncTaskState state) {
        getJdbcTemplate().update("UPDATE async_task SET state = ?, state_date = current_timestamp WHERE id = ?", state.getId(), id);
    }

    @Override
    public void delete(long id) {
        LOG.info("Deleting task: " + id);
        getJdbcTemplate().update("DELETE FROM async_task WHERE id = ?", id);
    }

    @Override
    public List<Integer> findUserIdsWaitingForTask(long id) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);
        return getNamedParameterJdbcTemplate().queryForList("SELECT user_id FROM async_task_subscribers WHERE async_task_id = :id " +
                "UNION SELECT user_id FROM async_task WHERE id = :id", params, Integer.class);
    }

    @Override
    public void addUserWaitingForTask(long asyncTaskId, int userId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("asyncTaskId", asyncTaskId);
        params.addValue("userId", userId);
        getNamedParameterJdbcTemplate().update("INSERT INTO async_task_subscribers (async_task_id, user_id) VALUES (:asyncTaskId, :userId)",
                params);
    }

    @Override
    public void releaseNodeTasks(String node) {
        LOG.info("Releasing tasks by node: " + node);
        getJdbcTemplate().update("UPDATE async_task SET node = NULL, start_process_date = NULL, state = ? WHERE node = ?",
                AsyncTaskState.IN_QUEUE.getId(), node);
    }

    @Override
    public List<Long> findAllByPriorityNode(String priorityNode) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("priorityNode", priorityNode);
        return getNamedParameterJdbcTemplate().queryForList("SELECT id FROM async_task WHERE priority_node = :priorityNode", params, Long.class);
    }

    @Override
    public boolean isTaskExists(long id) {
        return getJdbcTemplate().queryForObject("SELECT count(*) FROM async_task WHERE id = ?", Integer.class, id) != 0;
    }

    @Override
    public boolean isTaskActive(long id) {
        try {
            int state = getJdbcTemplate().queryForObject("SELECT state FROM async_task WHERE id = ?", Integer.class, id);
            return state != AsyncTaskState.CANCELLED.getId();
        } catch (DataAccessException e) {
            return false;
        }
    }

    @Override
    public AsyncTaskData findAsyncTaskByLockKey(String lockKey) {
        String query = "select * from async_task asy\n" +
                "left join lock_data ld on asy.id = ld.task_id\n" +
                "where ld.key = :key";
        MapSqlParameterSource params = new MapSqlParameterSource("key", lockKey);
        try {
            return getNamedParameterJdbcTemplate().queryForObject(query, params, new AsyncTaskDataMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}