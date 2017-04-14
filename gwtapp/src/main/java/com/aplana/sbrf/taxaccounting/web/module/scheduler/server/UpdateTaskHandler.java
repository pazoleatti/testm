package com.aplana.sbrf.taxaccounting.web.module.scheduler.server;

import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTask;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTaskData;
import com.aplana.sbrf.taxaccounting.model.util.StringUtils;
import com.aplana.sbrf.taxaccounting.service.api.ConfigurationService;
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
@PreAuthorize("hasAnyRole('N_ROLE_ADMIN')")
public class UpdateTaskHandler extends AbstractActionHandler<UpdateTaskAction, UpdateTaskResult> {

    @Autowired
    private ConfigurationService configurationService;
    @Autowired
    private SchedulerService schedulerService;

    public UpdateTaskHandler() {
        super(UpdateTaskAction.class);
    }

    @Override
    public UpdateTaskResult execute(UpdateTaskAction action, ExecutionContext executionContext) throws ActionException {
        UpdateTaskResult result = new UpdateTaskResult();
        SchedulerTaskData taskData = configurationService.getSchedulerTask(SchedulerTask.getByTaskId(action.getTaskId()));

        // список ошибок
        List<String> errors = new ArrayList<String>(1);

        /**
         * Проверка расписания
         */
        if (!configurationService.validateSchedule(action.getSchedule())){
            errors.add(" Значение атрибута «Расписание» не соответствует требованиям формата Cron!");
        }

        if (!errors.isEmpty()){
            result.setErrorMessage(StringUtils.join(errors.toArray(), '.'));
            result.setHasErrors(true);

            return result;
        }

        taskData.setParams(action.getParams());
        taskData.setSchedule(action.getSchedule());
        configurationService.updateTask(taskData);
        schedulerService.updateAllTask();
        return result;
    }

    @Override
    public void undo(UpdateTaskAction updateTaskAction, UpdateTaskResult updateTaskResult, ExecutionContext executionContext) throws ActionException {
        //do nothing
    }
}
