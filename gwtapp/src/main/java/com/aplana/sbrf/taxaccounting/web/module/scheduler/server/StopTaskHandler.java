package com.aplana.sbrf.taxaccounting.web.module.scheduler.server;

import com.aplana.sbrf.taxaccounting.scheduler.api.exception.TaskSchedulingException;
import com.aplana.sbrf.taxaccounting.scheduler.api.manager.TaskManager;
import com.aplana.sbrf.taxaccounting.web.module.scheduler.shared.StopTaskAction;
import com.aplana.sbrf.taxaccounting.web.module.scheduler.shared.StopTaskResult;
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
public class StopTaskHandler extends AbstractActionHandler<StopTaskAction, StopTaskResult> {

    @Autowired
    TaskManager taskManager;

    public StopTaskHandler() {
        super(StopTaskAction.class);
    }

    @Override
    public StopTaskResult execute(StopTaskAction action, ExecutionContext executionContext) throws ActionException {
        StopTaskResult result = new StopTaskResult();
        try {
            taskManager.stopTask(action.getTaskId());
        } catch (TaskSchedulingException e) {
            throw new ActionException("Ошибка остановки задачи планировщика", e);
        }
        return result;
    }

    @Override
    public void undo(StopTaskAction stopTaskAction, StopTaskResult stopTaskResult, ExecutionContext executionContext) throws ActionException {
        //do nothing
    }
}
