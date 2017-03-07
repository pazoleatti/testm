package com.aplana.sbrf.taxaccounting.web.module.scheduler.server;

import com.aplana.sbrf.taxaccounting.scheduler.api.exception.TaskSchedulingException;
import com.aplana.sbrf.taxaccounting.scheduler.api.manager.TaskManager;
import com.aplana.sbrf.taxaccounting.web.module.scheduler.shared.StartTaskAction;
import com.aplana.sbrf.taxaccounting.web.module.scheduler.shared.StartTaskResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * Запуск задачи планировщика
 * @author dloshkarev
 */
@Service
@PreAuthorize("hasAnyRole('N_ROLE_ADMIN')")
public class StartTaskHandler extends AbstractActionHandler<StartTaskAction, StartTaskResult> {

    @Autowired
    TaskManager taskManager;

    public StartTaskHandler() {
        super(StartTaskAction.class);
    }

    @Override
    public StartTaskResult execute(StartTaskAction action, ExecutionContext executionContext) throws ActionException {
        StartTaskResult result = new StartTaskResult();
        try {
            for (Long taskId : action.getTasksIds()) {
                taskManager.startTask(taskId);
            }
        } catch (TaskSchedulingException e) {
            throw new ActionException("Ошибка запуска задачи планировщика", e);
        }
        return result;
    }

    @Override
    public void undo(StartTaskAction startTaskAction, StartTaskResult startTaskResult, ExecutionContext executionContext) throws ActionException {
        //do nothing
    }
}
