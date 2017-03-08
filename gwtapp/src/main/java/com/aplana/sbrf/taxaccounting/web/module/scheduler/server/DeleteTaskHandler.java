package com.aplana.sbrf.taxaccounting.web.module.scheduler.server;

import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskData;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskState;
import com.aplana.sbrf.taxaccounting.scheduler.api.exception.TaskSchedulingException;
import com.aplana.sbrf.taxaccounting.scheduler.api.manager.TaskManager;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
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
@PreAuthorize("hasAnyRole('N_ROLE_ADMIN')")
public class DeleteTaskHandler extends AbstractActionHandler<DeleteTaskAction, DeleteTaskResult> {

    @Autowired
    TaskManager taskManager;

    @Autowired
    private LogEntryService logEntryService;

    public DeleteTaskHandler() {
        super(DeleteTaskAction.class);
    }

    @Override
    public DeleteTaskResult execute(DeleteTaskAction action, ExecutionContext executionContext) throws ActionException {
        DeleteTaskResult result = new DeleteTaskResult();
        Logger logger = new Logger();
        try {
            for (Long taskId : action.getTasksIds()) {
                TaskData taskData = taskManager.getTaskData(taskId);
                if (taskData.getTaskState() != TaskState.RUNNING){
                    taskManager.deleteTask(taskId);
                } else{
                    logger.error("Задача "+taskData.getTaskName()+" выполняется в данный момент и не может быть удалена.");
                }
            }
        } catch (TaskSchedulingException e) {
            throw new ActionException("Ошибка удаления задачи планировщика", e);
        }
        result.setUuid(logEntryService.save(logger.getEntries()));

        return result;
    }

    @Override
    public void undo(DeleteTaskAction deleteTaskAction, DeleteTaskResult deleteTaskResult, ExecutionContext executionContext) throws ActionException {
        //do nothing
    }
}
