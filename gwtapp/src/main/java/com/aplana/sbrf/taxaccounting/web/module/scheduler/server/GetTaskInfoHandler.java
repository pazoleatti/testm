package com.aplana.sbrf.taxaccounting.web.module.scheduler.server;

import com.aplana.sbrf.taxaccounting.model.TaskParamModel;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskData;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskParam;
import com.aplana.sbrf.taxaccounting.scheduler.api.exception.TaskSchedulingException;
import com.aplana.sbrf.taxaccounting.scheduler.api.manager.TaskManager;
import com.aplana.sbrf.taxaccounting.web.module.scheduler.shared.GetTaskInfoAction;
import com.aplana.sbrf.taxaccounting.web.module.scheduler.shared.GetTaskInfoResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

/**
 * Остановка задачи планировщика
 * @author dloshkarev
 */
@Service
@PreAuthorize("hasAnyRole('N_ROLE_ADMIN')")
public class GetTaskInfoHandler extends AbstractActionHandler<GetTaskInfoAction, GetTaskInfoResult> {

    @Autowired
    TaskManager taskManager;

    public GetTaskInfoHandler() {
        super(GetTaskInfoAction.class);
    }

    @Override
    public GetTaskInfoResult execute(GetTaskInfoAction action, ExecutionContext executionContext) throws ActionException {
        GetTaskInfoResult result = new GetTaskInfoResult();
        try {
            TaskData taskData = taskManager.getTaskData(action.getTaskId());
            result.setTaskId(taskData.getTaskId());
            result.setTaskName(taskData.getTaskName());
            result.setTaskState(taskData.getTaskState().getId());
            result.setSchedule(taskData.getSchedule());
            result.setUserTaskJndi(taskData.getUserTaskJndi());
            result.setTimeCreated(taskData.getTimeCreated());
            result.setNextFireTime(taskData.getNextFireTime());
            result.setRepeatsLeft(taskData.getRepeatsLeft());
            result.setContextId(taskData.getContextId());

            List<TaskParamModel> params = new ArrayList<TaskParamModel>();
            if (taskData.getParams() != null){
                for (TaskParam param : taskData.getParams().values()) {
                    TaskParamModel model = new TaskParamModel();
                    model.setTaskParamName(param.getName());
                    model.setTaskParamType(param.getType().getId());
                    model.setTaskParamValue(param.getValue());
                    params.add(model);
                }
            }
            result.setParams(params);
        } catch (TaskSchedulingException e) {
            throw new ActionException("Ошибка получения информации о задаче планировщика", e);
        }
        return result;
    }

    @Override
    public void undo(GetTaskInfoAction getTaskInfoAction, GetTaskInfoResult getTaskInfoResult, ExecutionContext executionContext) throws ActionException {
        //do nothing
    }
}
