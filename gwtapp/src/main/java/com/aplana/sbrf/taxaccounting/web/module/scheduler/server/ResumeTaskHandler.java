package com.aplana.sbrf.taxaccounting.web.module.scheduler.server;

import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskData;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskState;
import com.aplana.sbrf.taxaccounting.scheduler.api.exception.TaskSchedulingException;
import com.aplana.sbrf.taxaccounting.scheduler.api.manager.TaskManager;
import com.aplana.sbrf.taxaccounting.web.module.scheduler.shared.ResumeTaskAction;
import com.aplana.sbrf.taxaccounting.web.module.scheduler.shared.ResumeTaskResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * Возобновление задачи планировщика
 * @author dkoshkarev
 */
@Service
@PreAuthorize("hasAnyRole('N_ROLE_ADMIN')")
public class ResumeTaskHandler extends AbstractActionHandler<ResumeTaskAction, ResumeTaskResult> {

    @Autowired
    TaskManager taskManager;

    public ResumeTaskHandler() {
        super(ResumeTaskAction.class);
    }

    @Override
    public ResumeTaskResult execute(ResumeTaskAction action, ExecutionContext executionContext) throws ActionException {
        ResumeTaskResult result = new ResumeTaskResult();
        try {
            for (Long taskId : action.getTasksIds()) {
                TaskData taskData = taskManager.getTaskData(taskId);
                if (taskData.getTaskState() == TaskState.SUSPENDED){
                    taskManager.resumeTask(taskId);
                }
            }
        } catch (TaskSchedulingException e) {
            throw new ActionException("Ошибка возобновления задачи планировщика", e);
        }
        return result;
    }

    @Override
    public void undo(ResumeTaskAction action, ResumeTaskResult result, ExecutionContext executionContext) throws ActionException {
        //do nothing
    }
}