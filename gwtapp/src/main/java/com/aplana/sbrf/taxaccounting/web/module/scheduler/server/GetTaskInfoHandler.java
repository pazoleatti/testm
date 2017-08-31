package com.aplana.sbrf.taxaccounting.web.module.scheduler.server;

import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTask;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTaskData;
import com.aplana.sbrf.taxaccounting.service.api.ConfigurationService;
import com.aplana.sbrf.taxaccounting.web.module.scheduler.shared.GetTaskInfoAction;
import com.aplana.sbrf.taxaccounting.web.module.scheduler.shared.GetTaskInfoResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * Остановка задачи планировщика
 * @author dloshkarev
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
public class GetTaskInfoHandler extends AbstractActionHandler<GetTaskInfoAction, GetTaskInfoResult> {

    @Autowired
    private ConfigurationService configurationService;

    public GetTaskInfoHandler() {
        super(GetTaskInfoAction.class);
    }

    @Override
    public GetTaskInfoResult execute(GetTaskInfoAction action, ExecutionContext executionContext) throws ActionException {
        GetTaskInfoResult result = new GetTaskInfoResult();
        SchedulerTaskData taskData = configurationService.getSchedulerTask(SchedulerTask.getByTaskId(action.getTaskId()));
        result.setTaskId(taskData.getTask().getSchedulerTaskId());
        result.setTaskName(taskData.getTaskName());
        result.setTaskState(taskData.isActive()?0:1);
        result.setSchedule(taskData.getSchedule());
        result.setTimeCreated(taskData.getModificationDate());
        result.setNextFireTime(taskData.getLast_fire_date());
        result.setContextId(taskData.getTask().getSchedulerTaskId());
        result.setParams(taskData.getParams());
        return result;
    }

    @Override
    public void undo(GetTaskInfoAction getTaskInfoAction, GetTaskInfoResult getTaskInfoResult, ExecutionContext executionContext) throws ActionException {
        //do nothing
    }
}
