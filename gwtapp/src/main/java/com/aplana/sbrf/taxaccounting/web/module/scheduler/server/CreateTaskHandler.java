package com.aplana.sbrf.taxaccounting.web.module.scheduler.server;

import com.aplana.sbrf.taxaccounting.model.TaskParamModel;
import com.aplana.sbrf.taxaccounting.model.util.StringUtils;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskContext;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskParam;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskParamType;
import com.aplana.sbrf.taxaccounting.scheduler.api.exception.TaskSchedulingException;
import com.aplana.sbrf.taxaccounting.scheduler.api.manager.TaskManager;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.scheduler.shared.CreateTaskAction;
import com.aplana.sbrf.taxaccounting.web.module.scheduler.shared.CreateTaskResult;
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
@PreAuthorize("hasAnyRole('N_ROLE_ADMIN')")
public class CreateTaskHandler extends AbstractActionHandler<CreateTaskAction, CreateTaskResult> {

    @Autowired
    TaskManager taskManager;

    @Autowired
    SecurityService securityService;

    public CreateTaskHandler() {
        super(CreateTaskAction.class);
    }

    @Override
    public CreateTaskResult execute(CreateTaskAction action, ExecutionContext executionContext) throws ActionException {
        CreateTaskResult result = new CreateTaskResult();
        try {
            // список ошибок
            List<String> errors = new ArrayList<String>(2);

            /**
             * Проверка уникальности названия задачи
             */
            if (taskManager.isTaskExist(action.getTaskName())) {
                errors.add(" Название задачи не уникально!");
            }

            /**
             * Проверка расписания
             */
            if (!taskManager.validateSchedule(action.getSchedule())){
                errors.add(" Значение атрибута «Расписание» не соответствует требованиям формата Cron");
            }

            if (!errors.isEmpty()){
                result.setErrorMessage(StringUtils.join(errors.toArray(), '.'));
                result.setHasErrors(true);

                return result;
            }

            TaskContext taskContext = new TaskContext();
            taskContext.setTaskName(action.getTaskName());
            taskContext.setSchedule(action.getSchedule());
            taskContext.setUserTaskJndi(action.getUserTaskJndi());
            taskContext.setNumberOfRepeats(-1);
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

            taskManager.createTask(taskContext);

        } catch (TaskSchedulingException e) {
            throw new ActionException(e.getMessage(), e);
        }
        return result;
    }

    @Override
    public void undo(CreateTaskAction createTaskAction, CreateTaskResult createTaskResult, ExecutionContext executionContext) throws ActionException {
        //do nothing
    }
}
