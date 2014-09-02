package com.aplana.sbrf.taxaccounting.web.module.scheduler.server;

import com.aplana.sbrf.taxaccounting.model.TaskParamModel;
import com.aplana.sbrf.taxaccounting.model.util.StringUtils;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskContext;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskData;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskParam;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskParamType;
import com.aplana.sbrf.taxaccounting.scheduler.api.exception.TaskSchedulingException;
import com.aplana.sbrf.taxaccounting.scheduler.api.manager.TaskManager;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.scheduler.shared.UpdateTaskAction;
import com.aplana.sbrf.taxaccounting.web.module.scheduler.shared.UpdateTaskResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Создание задачи планировщика
 * @author dloshkarev
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
public class UpdateTaskHandler extends AbstractActionHandler<UpdateTaskAction, UpdateTaskResult> {

    @Autowired
    TaskManager taskManager;

    @Autowired
    SecurityService securityService;

    public UpdateTaskHandler() {
        super(UpdateTaskAction.class);
    }

    @Override
    public UpdateTaskResult execute(UpdateTaskAction action, ExecutionContext executionContext) throws ActionException {
        UpdateTaskResult result = new UpdateTaskResult();
        try {
            // нати в кеше
            TaskData taskData = taskManager.getTaskData(action.getTaskId());

            // список ошибок
            List<String> errors = new ArrayList<String>(2);

            /**
             * Проверка уникальности названия задачи
             */
            if (!taskData.getTaskName().equals(action.getTaskName()) && taskManager.isTaskExist(action.getTaskName())) {
                errors.add(" Название задачи не уникально!");
            }

            /**
             * Проверка расписания
             */
            if (!taskManager.validateSchedule(action.getSchedule())){
                errors.add(" Значение атрибута «Расписание» не соответствует требованиям формата Cron!!");
            }

            if (errors.size() > 0){
                result.setErrorMessage(StringUtils.join(errors.toArray(), '.'));
                result.setHasErrors(true);

                return result;
            }

            TaskContext taskContext = new TaskContext();
            taskContext.setTaskName(action.getTaskName());
            taskContext.setSchedule(action.getSchedule());
            taskContext.setUserTaskJndi(action.getUserTaskJndi());
            taskContext.setNumberOfRepeats(-1);
            taskContext.setId(action.getContextId());
            taskContext.setUserId(securityService.currentUserInfo().getUser().getId());

            Map<String, TaskParam> taskParams = new HashMap<String, TaskParam>();
            for (int i = 0; i < action.getParams().size(); i++) {
                TaskParamModel param = action.getParams().get(i);
                String paramValue = param.getTaskParamValue();
                taskParams.put(param.getTaskParamName(),
                        new TaskParam(i, param.getTaskParamName(),
                                TaskParamType.getTypeById(param.getTaskParamType()), paramValue));
            }
            taskContext.setParams(taskParams);

            taskManager.updateTask(action.getTaskId(), taskContext);

        } catch (TaskSchedulingException e) {
            throw new ActionException("Ошибка создания задачи планировщика", e);
        }

        return result;
    }

    @Override
    public void undo(UpdateTaskAction updateTaskAction, UpdateTaskResult updateTaskResult, ExecutionContext executionContext) throws ActionException {

    }
}
