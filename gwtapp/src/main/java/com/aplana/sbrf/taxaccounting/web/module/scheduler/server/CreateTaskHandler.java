package com.aplana.sbrf.taxaccounting.web.module.scheduler.server;

import com.aplana.sbrf.taxaccounting.model.TaskParamModel;
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

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Создание задачи планировщика
 * @author dloshkarev
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
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
            if (!taskManager.isTaskExist(action.getTaskName())) {
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
            } else {
                throw new ActionException("Название задачи не уникально!");
            }
        } catch (TaskSchedulingException e) {
            throw new ActionException(e.getMessage(), e);
        }
        return result;
    }

    @Override
    public void undo(CreateTaskAction CreateTaskAction, CreateTaskResult CreateTaskResult, ExecutionContext executionContext) throws ActionException {
        //do nothing
    }
}
