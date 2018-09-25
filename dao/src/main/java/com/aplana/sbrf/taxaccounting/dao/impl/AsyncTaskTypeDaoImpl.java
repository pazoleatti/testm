package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.AsyncTaskTypeDao;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskTypeData;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class AsyncTaskTypeDaoImpl extends AbstractDao implements AsyncTaskTypeDao {

    private static final class AsyncTaskTypeMapper implements RowMapper<AsyncTaskTypeData> {

        @Override
        public AsyncTaskTypeData mapRow(ResultSet rs, int rowNum) throws SQLException {
            AsyncTaskTypeData result = new AsyncTaskTypeData();
            result.setId(rs.getLong("id"));
            result.setName(rs.getString("name"));
            result.setHandlerClassName(rs.getString("handler_bean"));
            result.setShortQueueLimit(rs.getLong("short_queue_limit"));
            result.setTaskLimit(rs.getLong("task_limit"));
            result.setLimitKind(rs.getString("limit_kind"));
            return result;
        }
    }

    @Override
    public AsyncTaskTypeData findById(long id) {
        try {
            return getJdbcTemplate().queryForObject("SELECT id, name, handler_bean, short_queue_limit, task_limit, limit_kind FROM async_task_type WHERE id = ?",
                    new AsyncTaskTypeMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public PagingResult<AsyncTaskTypeData> findAll(PagingParams pagingParams) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("start", pagingParams.getStartIndex() + 1);
        params.addValue("end", pagingParams.getStartIndex() + pagingParams.getCount());
        List<AsyncTaskTypeData> asyncTaskTypeDataList = getNamedParameterJdbcTemplate().query(
                "SELECT * FROM (" +
                        "   SELECT rownum rn, ordered.* FROM (SELECT id, name, handler_bean, short_queue_limit, task_limit, limit_kind FROM async_task_type) ordered " +
                        ") numbered " +
                        "WHERE rn BETWEEN :start AND :end ORDER BY name",
                params, new AsyncTaskTypeMapper()
        );
        int totalCount = getJdbcTemplate().queryForObject("SELECT count(*) FROM (SELECT id, name, handler_bean, short_queue_limit, task_limit FROM async_task_type)", Integer.class);
        return new PagingResult<>(asyncTaskTypeDataList, totalCount);
    }

    @Override
    public void updateLimits(AsyncTaskTypeData asyncParam) {
        getJdbcTemplate().update("UPDATE ASYNC_TASK_TYPE SET TASK_LIMIT = ?, SHORT_QUEUE_LIMIT = ? WHERE ID = ?",
                asyncParam.getTaskLimit(), asyncParam.getShortQueueLimit(), asyncParam.getId()
        );
    }
}
