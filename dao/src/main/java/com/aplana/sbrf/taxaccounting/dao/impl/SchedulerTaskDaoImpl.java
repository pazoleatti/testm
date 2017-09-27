package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.SchedulerTaskDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.scheduler.*;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.QBean;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.sql.SQLQueryFactory;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static com.aplana.sbrf.taxaccounting.model.QConfigurationScheduler.configurationScheduler;
import static com.aplana.sbrf.taxaccounting.model.QConfigurationSchedulerParam.configurationSchedulerParam;
import static com.querydsl.core.types.Projections.bean;

/**
 * Дао для работы с типами асинхронных задач
 */
@Repository
public class SchedulerTaskDaoImpl extends AbstractDao implements SchedulerTaskDao {

    private final SQLQueryFactory sqlQueryFactory;

    //TODO разобраться как Byte преобразовать в Boolean
    private final QBean<SchedulerTaskModel> schedulerTaskModelBean = bean(SchedulerTaskModel.class, configurationScheduler.all());
    private final QBean<SchedulerTaskParamModel> schedulerTaskParamModelBean = bean(SchedulerTaskParamModel.class, configurationSchedulerParam.id, configurationSchedulerParam.paramName,
            configurationSchedulerParam.type, configurationSchedulerParam.value);

    @Autowired
    public SchedulerTaskDaoImpl(SQLQueryFactory sqlQueryFactory) {
        this.sqlQueryFactory = sqlQueryFactory;
    }

    @Override
    public SchedulerTaskData get(Long taskId) {
        List<SchedulerTaskModel> schedulerTaskModelList = sqlQueryFactory.from(configurationScheduler)
                .where(configurationScheduler.id.eq(taskId.intValue()))
                .transform(GroupBy.groupBy(configurationScheduler.id).list(schedulerTaskModelBean));

        if (!schedulerTaskModelList.isEmpty()) {
            SchedulerTaskModel schedulerTaskModel = schedulerTaskModelList.get(0);
            SchedulerTaskData schedulerTaskData = new SchedulerTaskData();
            schedulerTaskData.setTask(SchedulerTask.getByTaskId(schedulerTaskModel.getId()));
            schedulerTaskData.setTaskName(schedulerTaskModel.getTaskName());
            schedulerTaskData.setSchedule(schedulerTaskModel.getSchedule());
            schedulerTaskData.setModificationDate(schedulerTaskModel.getModificationDate());
            schedulerTaskData.setLast_fire_date(schedulerTaskModel.getLastFireDate());
            schedulerTaskData.setActive(schedulerTaskModel.getActive() == 1);
            schedulerTaskData.setParams(getTaskParam(taskId));
            return schedulerTaskData;
        }

        return null;
    }

    @Override
    public List<SchedulerTaskData> getAll() {
        List<SchedulerTaskModel> schedulerTaskModelList = sqlQueryFactory.from(configurationScheduler)
                .transform(GroupBy.groupBy(configurationScheduler.id).list(schedulerTaskModelBean));

        List<SchedulerTaskData> schedulerTaskDataList = new ArrayList<SchedulerTaskData>();

        if (!schedulerTaskModelList.isEmpty()) {
            for (SchedulerTaskModel task : schedulerTaskModelList) {
                SchedulerTaskData schedulerTaskData = new SchedulerTaskData();
                schedulerTaskData.setTask(SchedulerTask.getByTaskId(task.getId()));
                schedulerTaskData.setTaskName(task.getTaskName());
                schedulerTaskData.setSchedule(task.getSchedule());
                schedulerTaskData.setModificationDate(task.getModificationDate());
                schedulerTaskData.setLast_fire_date(task.getLastFireDate());
                schedulerTaskData.setActive(task.getActive() == 1);
                schedulerTaskDataList.add(schedulerTaskData);
            }
            return schedulerTaskDataList;
        }

        return null;
    }

    @Override
    public PagingResult<SchedulerTaskModel> getAllWithPaging(PagingParams pagingParams) {
        List<SchedulerTaskModel> schedulerTaskModelList = sqlQueryFactory.from(configurationScheduler)
                .offset(pagingParams.getStartIndex())
                .limit(pagingParams.getCount())
                .transform(GroupBy.groupBy(configurationScheduler.id).list(schedulerTaskModelBean));

        int totalCount = (int) sqlQueryFactory.from(configurationScheduler)
                .fetchCount();
        return new PagingResult<SchedulerTaskModel>(schedulerTaskModelList, totalCount);
    }

    @Override
    public void updateTask(SchedulerTaskData taskData) {
        sqlQueryFactory.update(configurationScheduler)
                .where(configurationScheduler.id.eq((int) taskData.getTask().getSchedulerTaskId()))
                .set(configurationScheduler.schedule, taskData.getSchedule())
                .set(configurationScheduler.modificationDate, DateExpression.currentDate(LocalDateTime.class))
                .execute();

        updateTaskParam(taskData.getParams());
    }

    @Override
    public void updateTaskStartDate(long taskId) {
        sqlQueryFactory.update(configurationScheduler)
                .where(configurationScheduler.id.eq((int) taskId))
                .set(configurationScheduler.lastFireDate, DateExpression.currentDate(LocalDateTime.class))
                .execute();
    }

    @Override
    public void setActiveSchedulerTask(boolean active, List<Long> ids) {
        if (!ids.isEmpty()) {
            List<Integer> idsInt = new ArrayList<Integer>();
            for (Long id : ids) {
                idsInt.add(id.intValue());
            }
            sqlQueryFactory.update(configurationScheduler)
                    .where(configurationScheduler.id.in(idsInt))
                    .set(configurationScheduler.active, active ? (byte) 1 : (byte) 0)
                    .execute();
        }
    }

    private List<SchedulerTaskParam> getTaskParam(long taskId) {
        List<SchedulerTaskParamModel> schedulerTaskParamModelList = sqlQueryFactory.from(configurationSchedulerParam)
                .where(configurationSchedulerParam.taskId.eq((int) taskId))
                .orderBy(configurationSchedulerParam.ord.asc())
                .transform(GroupBy.groupBy(configurationSchedulerParam.id).list(schedulerTaskParamModelBean));

        List<SchedulerTaskParam> schedulerTaskParamList = new ArrayList<SchedulerTaskParam>();

        if (!schedulerTaskParamModelList.isEmpty()) {
            for (SchedulerTaskParamModel taskParamModel : schedulerTaskParamModelList) {
                SchedulerTaskParam taskData = new SchedulerTaskParam();
                taskData.setId(taskParamModel.getId());
                taskData.setParamName(taskParamModel.getParamName());
                taskData.setParamType(SchedulerTaskParamType.getById(taskParamModel.getType()));
                taskData.setValue(taskParamModel.getValue());
                schedulerTaskParamList.add(taskData);
            }

        }

        return schedulerTaskParamList;
    }

    private void updateTaskParam(final List<SchedulerTaskParam> params) {
        if (!params.isEmpty()) {
            for (SchedulerTaskParam schedulerTaskParam : params) {
                sqlQueryFactory.update(configurationSchedulerParam)
                        .where(configurationSchedulerParam.id.eq((int) schedulerTaskParam.getId()))
                        .set(configurationSchedulerParam.value, schedulerTaskParam.getValue())
                        .execute();
            }
        }
    }
}
