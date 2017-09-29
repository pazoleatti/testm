package com.aplana.sbrf.taxaccounting.web.module.scheduler.server;

import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTask;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTaskData;
import com.aplana.sbrf.taxaccounting.model.util.StringUtils;
import com.aplana.sbrf.taxaccounting.service.api.ConfigurationService;
import com.aplana.sbrf.taxaccounting.service.api.SchedulerTaskService;
import com.aplana.sbrf.taxaccounting.service.scheduler.SchedulerService;
import com.aplana.sbrf.taxaccounting.web.module.scheduler.shared.UpdateTaskAction;
import com.aplana.sbrf.taxaccounting.web.module.scheduler.shared.UpdateTaskResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Создание задачи планировщика
 * @author dloshkarev
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
public class UpdateTaskHandler extends AbstractActionHandler<UpdateTaskAction, UpdateTaskResult> {

    @Autowired
    private SchedulerTaskService schedulerTaskService;
    @Autowired
    private SchedulerService schedulerService;

    public UpdateTaskHandler() {
        super(UpdateTaskAction.class);
    }

    @Override
    public UpdateTaskResult execute(UpdateTaskAction action, ExecutionContext executionContext) throws ActionException {
        UpdateTaskResult result = new UpdateTaskResult();
        SchedulerTaskData taskData = schedulerTaskService.getSchedulerTask(SchedulerTask.getByTaskId(action.getTaskId()));

        // список ошибок
        List<String> errors = new ArrayList<String>(1);

        /**
         * Проверка расписания
         */
        if (!schedulerTaskService.validateSchedule(action.getSchedule())){
            errors.add(" Значение атрибута «Расписание» не соответствует требованиям формата Cron!");
        }

        if (!errors.isEmpty()){
            result.setErrorMessage(StringUtils.join(errors.toArray(), '.'));
            result.setHasErrors(true);

            return result;
        }

        taskData.setParams(action.getParams());
        taskData.setSchedule(action.getSchedule());
        schedulerTaskService.updateTask(taskData);
        schedulerService.updateAllTask();
        return result;
    }

    @Override
    public void undo(UpdateTaskAction updateTaskAction, UpdateTaskResult updateTaskResult, ExecutionContext executionContext) throws ActionException {
        //do nothing
    }
}
