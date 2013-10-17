package com.aplana.sbrf.taxaccounting.web.module.scheduler.server;

import com.aplana.sbrf.taxaccounting.scheduler.api.exception.TaskSchedulingException;
import com.aplana.sbrf.taxaccounting.scheduler.api.manager.TaskManager;
import com.aplana.sbrf.taxaccounting.web.module.scheduler.shared.DeleteTaskAction;
import com.aplana.sbrf.taxaccounting.web.module.scheduler.shared.DeleteTaskResult;
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
public class DeleteTaskHandler extends AbstractActionHandler<DeleteTaskAction, DeleteTaskResult> {

    @Autowired
    TaskManager taskManager;

    public DeleteTaskHandler() {
        super(DeleteTaskAction.class);
    }

    @Override
    public DeleteTaskResult execute(DeleteTaskAction action, ExecutionContext executionContext) throws ActionException {
        DeleteTaskResult result = new DeleteTaskResult();
        try {
            taskManager.deleteTask(action.getTaskId());
        } catch (TaskSchedulingException e) {
            throw new ActionException("Ошибка удаления задачи планировщика", e);
        }
        return result;
    }

    @Override
    public void undo(DeleteTaskAction DeleteTaskAction, DeleteTaskResult DeleteTaskResult, ExecutionContext executionContext) throws ActionException {
        //do nothing
    }
}
