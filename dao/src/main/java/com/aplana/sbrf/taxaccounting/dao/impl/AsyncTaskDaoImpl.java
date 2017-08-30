package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.async.AsyncTask;
import com.aplana.sbrf.taxaccounting.dao.AsyncTaskDao;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskData;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskTypeData;
import com.aplana.sbrf.taxaccounting.model.BalancingVariants;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

/**
 * Дао для работы с асинхронными задачами
 *
 * @author dloshkarev
 */
@Repository
public class AsyncTaskDaoImpl extends AbstractDao implements AsyncTaskDao {

    private static final Log LOG = LogFactory.getLog(AsyncTaskDaoImpl.class);

    private static final class AsyncTaskTypeMapper implements RowMapper<AsyncTaskTypeData> {

        @Override
        public AsyncTaskTypeData mapRow(ResultSet rs, int rowNum) throws SQLException {
            AsyncTaskTypeData taskType = new AsyncTaskTypeData();
            taskType.setId(rs.getLong("id"));
            taskType.setName(rs.getString("name"));
            taskType.setHandlerClassName(rs.getString("handler_bean"));
            taskType.setShortQueueLimit(rs.getLong("short_queue_limit"));
            taskType.setTaskLimit(rs.getLong("task_limit"));
            return taskType;
        }
    }

    @Override
    public AsyncTaskTypeData getTaskData(Long asyncTaskTypeId) {
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
    public void addTask(long taskTypeId, BalancingVariants balancingVariant, Map<String, Object> params) {
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

        getJdbcTemplate().update("insert into async_task (id, type_id, balancing_variant, serialized_params) values (seq_async_task.nextval, ?, ?, ?)",
                taskTypeId, balancingVariant.getId(), serializedParams);
    }

    @Override
    public int lockTask(String node, int timeout, BalancingVariants balancingVariants, int maxTasksPerNode) {
        return getJdbcTemplate().update("update async_task set node = ?, start_process_date = current_timestamp where (select count(*) from async_task where node = ?) < ? and id = (select id from (" +
                        "select * from async_task where balancing_variant = ? and (node is null or current_timestamp > start_process_date + interval '" + timeout + "' hour) order by create_date" +
                        ") where rownum = 1)",
                node, node, maxTasksPerNode, balancingVariants.getId());
    }

    @Override
    public AsyncTaskData getLockedTask(String node, BalancingVariants balancingVariants) {
        try {
            return getJdbcTemplate().queryForObject("select * from (" +
                    "select id, type_id, create_date, node, serialized_params from async_task where node = ? and balancing_variant = ? order by start_process_date desc" +
                    ") where rownum = 1", new RowMapper<AsyncTaskData>() {
                @Override
                public AsyncTaskData mapRow(ResultSet rs, int i) throws SQLException {
                    AsyncTaskData result = new AsyncTaskData();
                    result.setId(rs.getLong("id"));
                    result.setTypeId(rs.getLong("type_id"));
                    result.setCreateDate(rs.getDate("create_date"));
                    result.setNode(rs.getString("node"));
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
            }, node, balancingVariants.getId());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void finishTask(AsyncTaskData taskData) {
        if (taskData != null) {
            LOG.info("Finishing task: " + taskData);
            getJdbcTemplate().update("delete from async_task where id = ?", taskData.getId());
            getJdbcTemplate().update("delete from lock_data where key = ?", taskData.getParams().get(AsyncTask.RequiredParams.LOCKED_OBJECT));
        }
    }

    @Override
    public void releaseTask(long taskId) {
        getJdbcTemplate().update("update async_task set node = null, start_process_date = null where id = ?", taskId);
    }

}
