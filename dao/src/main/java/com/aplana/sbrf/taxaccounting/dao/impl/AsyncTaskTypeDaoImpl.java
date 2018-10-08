package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.AsyncTaskTypeDao;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskTypeData;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AsyncTaskTypeDaoImpl extends AbstractDao implements AsyncTaskTypeDao {

    @Override
    public AsyncTaskTypeData findById(long id) {
        try {
            return getJdbcTemplate().queryForObject("SELECT id, name, handler_bean, short_queue_limit, task_limit, limit_kind FROM async_task_type WHERE id = ?",
                    new BeanPropertyRowMapper<>(AsyncTaskTypeData.class), id);
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
                params, new BeanPropertyRowMapper<>(AsyncTaskTypeData.class)
        );
        int totalCount = getJdbcTemplate().queryForObject("SELECT count(*) FROM (SELECT id, name, handler_bean, short_queue_limit, task_limit FROM async_task_type)", Integer.class);
        return new PagingResult<>(asyncTaskTypeDataList, totalCount);
    }

    @Override
    public void updateLimits(long id, Long shortQueueLimit, Long taskLimit) {
        getJdbcTemplate().update("UPDATE ASYNC_TASK_TYPE SET TASK_LIMIT = ?, SHORT_QUEUE_LIMIT = ? WHERE ID = ?",
                taskLimit, shortQueueLimit, id
        );
    }
}
