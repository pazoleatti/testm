package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.SchedulerTaskDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTask;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTaskData;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTaskParam;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTaskParamType;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Дао для работы с типами асинхронных задач
 */
@Repository
public class SchedulerTaskDaoImpl extends AbstractDao implements SchedulerTaskDao {

    /**
     * Маппер для представления значений из {@link ResultSet} в виде объекта {@link SchedulerTaskData}
     */
    private static final class SchedulerTaskMapper implements RowMapper<SchedulerTaskData> {
        @Override
        public SchedulerTaskData mapRow(ResultSet rs, int rowNum) throws SQLException {
            SchedulerTaskData taskData = new SchedulerTaskData();
            taskData.setTask(SchedulerTask.getByTaskId(rs.getLong("id")));
            taskData.setTaskName(rs.getString("task_name"));
            taskData.setSchedule(rs.getString("schedule"));
            taskData.setModificationDate(new Date(rs.getTimestamp("modification_date").getTime()));
            Timestamp last_fire_date = rs.getTimestamp("last_fire_date");
            if (last_fire_date != null) {
                taskData.setLastFireDate(new Date(last_fire_date.getTime()));
            }
            taskData.setActive(rs.getBoolean("active"));
            return taskData;
        }
    }

    /**
     * Маппер для представления значений из {@link ResultSet} в виде объекта {@link SchedulerTaskParam}
     */
    private static final class SchedulerTaskParamMapper implements RowMapper<SchedulerTaskParam> {
        @Override
        public SchedulerTaskParam mapRow(ResultSet rs, int rowNum) throws SQLException {
            SchedulerTaskParam taskData = new SchedulerTaskParam();
            taskData.setId(rs.getLong("id"));
            taskData.setParamName(rs.getString("param_name"));
            taskData.setParamType(SchedulerTaskParamType.getById(rs.getInt("type")));
            taskData.setValue(rs.getString("value"));
            return taskData;
        }
    }

    @Override
    public SchedulerTaskData fetchOne(Long taskId) {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("taskId", taskId);
            SchedulerTaskData taskData = getNamedParameterJdbcTemplate().queryForObject(
                    "SELECT id, task_name, schedule, active, modification_date, last_fire_date " +
                            "FROM CONFIGURATION_SCHEDULER WHERE id = :taskId",
                    params, new SchedulerTaskMapper()
            );
            taskData.setParams(fetchAllParams(taskId));
            return taskData;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private final static String SELECT_ALL = "select id, task_name, schedule, active, modification_date, last_fire_date " +
            "from CONFIGURATION_SCHEDULER order by id";

    @Override
    public List<SchedulerTaskData> fetchAll() {
        return getJdbcTemplate().query(SELECT_ALL, new SchedulerTaskMapper());
    }

    @Override
    public PagingResult<SchedulerTaskData> fetchAllByPaging(PagingParams pagingParams) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("start", pagingParams.getStartIndex() + 1);
        params.addValue("end", pagingParams.getStartIndex() + pagingParams.getCount());
        List<SchedulerTaskData> schedulerTaskDataList = getNamedParameterJdbcTemplate().query(
                "select * from (" +
                        "   select rownum rn, ordered.* from (" + SELECT_ALL + ") ordered " +
                        ") numbered " +
                        "where rn between :start and :end",
                params, new SchedulerTaskMapper()
        );
        int totalCount = getJdbcTemplate().queryForObject("select count(*) from (" + SELECT_ALL + ")", Integer.class);
        return new PagingResult<>(schedulerTaskDataList, totalCount);
    }

    @Override
    public void update(SchedulerTaskData taskData) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("schedule", taskData.getSchedule());
        params.addValue("taskId", taskData.getTask().getSchedulerTaskId());
        params.addValue("modification_date", new Date());
        getNamedParameterJdbcTemplate().update(
                "UPDATE CONFIGURATION_SCHEDULER SET schedule = :schedule, MODIFICATION_DATE = :modification_date WHERE id = :taskId",
                params
        );
        updateTaskParam(taskData.getParams());
    }

    @Override
    public void updateStartDate(long taskId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("taskId", taskId);
        params.addValue("modification_date", new Date());
        getNamedParameterJdbcTemplate().update(
                "UPDATE CONFIGURATION_SCHEDULER SET last_fire_date = :modification_date WHERE id = :taskId",
                params
        );
    }

    @Override
    public void updateActiveByIds(boolean active, List<Long> ids) {
        if (!ids.isEmpty()) {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("active", active);
            params.addValue("ids", ids);
            getNamedParameterJdbcTemplate().update(
                    "UPDATE CONFIGURATION_SCHEDULER SET active = :active WHERE id in (:ids)",
                    params
            );
        }
    }

    private List<SchedulerTaskParam> fetchAllParams(long taskId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("taskId", taskId);
        return getNamedParameterJdbcTemplate().query(
                "SELECT id, param_name, type, value " +
                        "FROM CONFIGURATION_SCHEDULER_PARAM " +
                        "WHERE task_id = :taskId " +
                        "ORDER BY ord",
                params, new SchedulerTaskParamMapper()
        );
    }

    private void updateTaskParam(final List<SchedulerTaskParam> params) {
        if (params != null && !params.isEmpty()) {
            List<MapSqlParameterSource> paramsList = new ArrayList<>();
            for (SchedulerTaskParam param : params) {
                paramsList.add(new MapSqlParameterSource()
                        .addValue("value", param.getValue())
                        .addValue("id", param.getId()));
            }
            getNamedParameterJdbcTemplate().batchUpdate(
                    "UPDATE CONFIGURATION_SCHEDULER_PARAM SET value = ? " +
                            "WHERE id = ?",
                    (MapSqlParameterSource[]) paramsList.toArray());
        }
    }
}
