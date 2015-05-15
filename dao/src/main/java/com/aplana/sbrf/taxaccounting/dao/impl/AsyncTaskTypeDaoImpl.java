package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.AsyncTaskTypeDao;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskTypeData;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.*;

/**
 * Дао для работы с типами асинхронных задач
 */
@Repository
public class AsyncTaskTypeDaoImpl extends AbstractDao implements AsyncTaskTypeDao {

    private static final class AsyncTaskTypeMapper implements RowMapper<AsyncTaskTypeData>{

        @Override
        public AsyncTaskTypeData mapRow(ResultSet rs, int rowNum) throws SQLException {
            AsyncTaskTypeData taskType = new AsyncTaskTypeData();
            taskType.setId(rs.getLong("id"));
            taskType.setName(rs.getString("name"));
            taskType.setHandlerJndi(rs.getString("handler_jndi"));
            taskType.setShortQueueLimit(rs.getLong("short_queue_limit"));
            taskType.setTaskLimit(rs.getLong("task_limit"));
            return taskType;
        }
    }

    @Override
    public AsyncTaskTypeData get(Long asyncTaskTypeId) {
        try{
            return getJdbcTemplate().queryForObject("SELECT id, name, handler_jndi, short_queue_limit, task_limit FROM async_task_type WHERE id = ?",
                    new Object[]{asyncTaskTypeId},
                    new int[]{Types.INTEGER},
                    new AsyncTaskTypeMapper());
        }catch (EmptyResultDataAccessException e){
            return null;
        }
    }

}
