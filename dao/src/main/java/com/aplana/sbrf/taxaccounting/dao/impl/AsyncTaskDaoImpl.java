package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.AsyncTaskDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.util.DBUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.sql.Types;
import java.util.List;
import java.util.Map;

/**
 * Дао для работы с асинхронными задачами
 *
 * @author dloshkarev
 */
@Repository
public class AsyncTaskDaoImpl extends AbstractDao implements AsyncTaskDao {

    private static final Log LOG = LogFactory.getLog(AsyncTaskDaoImpl.class);

    @Autowired
    private DBUtils dbUtils;

    private static final class AsyncTaskTypeMapper implements RowMapper<AsyncTaskTypeData> {

        @Override
        public AsyncTaskTypeData mapRow(ResultSet rs, int rowNum) throws SQLException {
            AsyncTaskTypeData result = new AsyncTaskTypeData();
            result.setId(rs.getLong("id"));
            result.setName(rs.getString("name"));
            result.setHandlerClassName(rs.getString("handler_bean"));
            result.setShortQueueLimit(rs.getLong("short_queue_limit"));
            result.setTaskLimit(rs.getLong("task_limit"));
            return result;
        }
    }

    private static final class AsyncTaskMapper implements RowMapper<AsyncTaskData> {
        /* Признак того, что надо заполнить все поля включая сериализованные параметры */
        private boolean full;

        public AsyncTaskMapper(boolean full) {
            this.full = full;
        }

        @Override
        public AsyncTaskData mapRow(ResultSet rs, int rowNum) throws SQLException {
            AsyncTaskData result = new AsyncTaskData();
            result.setId(rs.getLong("id"));
            result.setType(AsyncTaskType.getByAsyncTaskTypeId(rs.getInt("type_id")));
            result.setUserId(rs.getInt("user_id"));
            result.setCreateDate(rs.getDate("create_date"));
            result.setNode(rs.getString("node"));
            result.setQueue(AsyncQueue.getById(rs.getInt("queue")));
            result.setState(AsyncTaskState.getById(rs.getInt("state")));
            result.setStateDate(rs.getDate("state_date"));
            result.setDescription(rs.getString("description"));
            if (full) {
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
            }
            return result;
        }
    }

    @Override
    public AsyncTaskTypeData getTaskTypeData(long asyncTaskTypeId) {
        try {
            return getJdbcTemplate().queryForObject("SELECT id, name, handler_bean, short_queue_limit, task_limit FROM async_task_type WHERE id = ?",
                    new Object[]{asyncTaskTypeId},
                    new int[]{Types.INTEGER},
                    new AsyncTaskTypeMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public AsyncTaskData addTask(long taskTypeId, int userId, String description, AsyncQueue queue, String priorityNode, Map<String, Object> params) {
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

        getJdbcTemplate().update("insert into async_task (id, type_id, user_id, description, queue, priority_node, serialized_params) values (?, ?, ?, ?, ?, ?, ?)",
                id, taskTypeId, userId, description, queue.getId(), priorityNode, serializedParams);
        return getTaskData(id);
    }

    @Override
    public int lockTask(String node, int timeout, AsyncQueue queue, int maxTasksPerNode) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("node", node);
        params.addValue("maxTasksPerNode", maxTasksPerNode);
        params.addValue("queue", queue.getId());
        return getNamedParameterJdbcTemplate().update("update async_task set node = :node, state_date = current_timestamp, start_process_date = current_timestamp where (select count(*) from async_task where node = :node) < :maxTasksPerNode and id = (select id from (" +
                        "select * from async_task where (priority_node is null or priority_node = :node) and queue = :queue and (node is null or current_timestamp > start_process_date + interval '" + timeout + "' hour) order by create_date" +
                        ") where rownum = 1)",
                params);
    }

    @Override
    public AsyncTaskData getLockedTask(String node, AsyncQueue queue) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("node", node);
        params.addValue("queue", queue.getId());
        try {
            return getNamedParameterJdbcTemplate().queryForObject("select * from (" +
                    "select * from async_task where node = :node and queue = :queue order by start_process_date desc" +
                    ") where rownum = 1", params, new AsyncTaskMapper(true));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public AsyncTaskData getTaskData(long taskId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("taskId", taskId);
        try {
            return getNamedParameterJdbcTemplate().queryForObject("select * from async_task where id = :taskId", params, new AsyncTaskMapper(true));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public AsyncTaskData getLightTaskData(long taskId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("taskId", taskId);
        try {
            return getNamedParameterJdbcTemplate().queryForObject("select * from async_task where id = :taskId", params, new AsyncTaskMapper(false));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void updateState(long taskId, AsyncTaskState state) {
        getJdbcTemplate().update("update async_task set state = ?, state_date = current_timestamp where id = ?", state.getId(), taskId);
    }

    @Override
    public void finishTask(long taskId) {
        LOG.info("Finishing task: " + taskId);
        getJdbcTemplate().update("delete from async_task where id = ?", taskId);
    }

    @Override
    public void cancelTask(long taskId) {
        LOG.info("Cancelling task: " + taskId);
        getJdbcTemplate().update("update async_task set state = ? where id = ?", AsyncTaskState.CANCELLED.getId(), taskId);
    }

    @Override
    public void releaseTask(long taskId) {
        getJdbcTemplate().update("update async_task set node = null, start_process_date = null where id = ?", taskId);
    }

    @Override
    public boolean isTaskActive(long taskId) {
        try {
            int state = getJdbcTemplate().queryForObject("select state from async_task where id = ?", Integer.class, taskId);
            return state != AsyncTaskState.CANCELLED.getId();
        } catch (DataAccessException e) {
            return false;
        }
    }

    @Override
    public List<Integer> getUsersWaitingForTask(long taskId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("taskId", taskId);
        return getNamedParameterJdbcTemplate().queryForList("select user_id from async_task_subscribers where async_task_id = :taskId " +
                "union select user_id from async_task where id = :taskId", params, Integer.class);
    }

    @Override
    public void addUserWaitingForTask(long taskId, int userId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("taskId", taskId);
        params.addValue("userId", userId);
        getNamedParameterJdbcTemplate().update("insert into async_task_subscribers (async_task_id, user_id) values (:taskId, :userId)", params);
    }

}
