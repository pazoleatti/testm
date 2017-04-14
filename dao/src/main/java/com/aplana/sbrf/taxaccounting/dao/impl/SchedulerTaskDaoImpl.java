package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.SchedulerTaskDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTask;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTaskData;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTaskParam;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTaskParamType;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

/**
 * Дао для работы с типами асинхронных задач
 */
@Repository
public class SchedulerTaskDaoImpl extends AbstractDao implements SchedulerTaskDao {

    private static final class SchedulerTaskMapper implements RowMapper<SchedulerTaskData>{

        @Override
        public SchedulerTaskData mapRow(ResultSet rs, int rowNum) throws SQLException {
            SchedulerTaskData taskData = new SchedulerTaskData();
            taskData.setTask(SchedulerTask.getByTaskId(rs.getLong("id")));
            taskData.setTaskName(rs.getString("task_name"));
            taskData.setSchedule(rs.getString("schedule"));
            taskData.setModificationDate(new Date(rs.getTimestamp("modification_date").getTime()));
            Timestamp start_date = rs.getTimestamp("start_date");
            if (start_date != null) {
                taskData.setStartDate(new Date(start_date.getTime()));
            }
            taskData.setActive(rs.getBoolean("active"));
            return taskData;
        }
    }

    private static final class SchedulerTaskParamMapper implements RowMapper<SchedulerTaskParam>{

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
    public SchedulerTaskData get(Long taskId) {
        try{
            Map<String, Object> valueMap = new HashMap<String, Object>();
            valueMap.put("taskId", taskId);
            SchedulerTaskData taskData = getNamedParameterJdbcTemplate().queryForObject(
                    "SELECT id, task_name, schedule, active, modification_date, start_date " +
                            "FROM CONFIGURATION_SCHEDULER WHERE id = :taskId",
                    valueMap,
                    new SchedulerTaskMapper()
            );
            taskData.setParams(getTaskParam(taskId));
            return taskData;
        } catch (EmptyResultDataAccessException e){
            return null;
        }
    }

    @Override
    public List<SchedulerTaskData> getAll() {
        try{
            return getJdbcTemplate().query(
                    "SELECT id, task_name, schedule, active, modification_date, start_date " +
                            "FROM CONFIGURATION_SCHEDULER ORDER BY id",
                    new SchedulerTaskMapper()
            );
        } catch (EmptyResultDataAccessException e){
            return null;
        }
    }

    @Override
    public void updateTask(SchedulerTaskData taskData) {
        Map<String, Object> valueMap = new HashMap<String, Object>();
        valueMap.put("schedule", taskData.getSchedule());
        valueMap.put("taskId", taskData.getTask().getSchedulerTaskId());
        getNamedParameterJdbcTemplate().update(
                "UPDATE CONFIGURATION_SCHEDULER SET schedule = :schedule, MODIFICATION_DATE = CURRENT_DATE WHERE id = :taskId",
                valueMap
        );
        updateTaskParam(taskData.getParams());
    }

    @Override
    public void updateTaskStartDate(long taskId) {
        Map<String, Object> valueMap = new HashMap<String, Object>();
        valueMap.put("taskId", taskId);
        getNamedParameterJdbcTemplate().update(
                "UPDATE CONFIGURATION_SCHEDULER SET START_DATE = CURRENT_DATE WHERE id = :taskId",
                valueMap
        );
    }

    @Override
    public void setActiveSchedulerTask(boolean active, List<Long> ids) {
        if (!ids.isEmpty()) {
            Map<String, Object> valueMap = new HashMap<String, Object>();
            valueMap.put("active", active);
            getNamedParameterJdbcTemplate().update(
                    String.format("UPDATE CONFIGURATION_SCHEDULER SET active = :active WHERE %s", SqlUtils.transformToSqlInStatement("id", ids)),
                    valueMap
            );
        }
    }

    private List<SchedulerTaskParam> getTaskParam(long taskId) {
        try{
            Map<String, Object> valueMap = new HashMap<String, Object>();
            valueMap.put("taskId", taskId);
            return getNamedParameterJdbcTemplate().query(
                    "SELECT id, param_name, type, value " +
                            "FROM CONFIGURATION_SCHEDULER_PARAM " +
                            "WHERE task_id = :taskId " +
                            "ORDER BY ord",
                    valueMap,
                    new SchedulerTaskParamMapper()
            );
        } catch (EmptyResultDataAccessException e){
            return new ArrayList<SchedulerTaskParam>();
        }
    }

    private void updateTaskParam(final List<SchedulerTaskParam> params) {
        if (params != null && !params.isEmpty()) {
            getJdbcTemplate().batchUpdate(
                    "UPDATE CONFIGURATION_SCHEDULER_PARAM SET value = ? " +
                            "WHERE id = ?",
                    new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                            SchedulerTaskParam schedulerTaskParam = params.get(i);
                            preparedStatement.setString(1, schedulerTaskParam.getValue());
                            preparedStatement.setLong(2, schedulerTaskParam.getId());
                        }

                        @Override
                        public int getBatchSize() {
                            return params.size();
                        }
                    }
            );
        }
    }
}
