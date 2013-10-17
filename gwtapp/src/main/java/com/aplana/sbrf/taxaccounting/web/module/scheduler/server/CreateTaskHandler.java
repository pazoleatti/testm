package com.aplana.sbrf.taxaccounting.web.module.scheduler.server;

import com.aplana.sbrf.taxaccounting.model.TaskParamModel;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskContext;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskParam;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskParamType;
import com.aplana.sbrf.taxaccounting.scheduler.api.exception.TaskSchedulingException;
import com.aplana.sbrf.taxaccounting.scheduler.api.manager.TaskManager;
import com.aplana.sbrf.taxaccounting.scheduler.api.utils.CronUtils;
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

    public CreateTaskHandler() {
        super(CreateTaskAction.class);
    }

    @Override
    public CreateTaskResult execute(CreateTaskAction action, ExecutionContext executionContext) throws ActionException {
        SimpleDateFormat df = new SimpleDateFormat(TaskParam.DATE_FORMAT);
        CreateTaskResult result = new CreateTaskResult();
        try {
            if (!taskManager.isTaskExist(action.getTaskName())) {
                TaskContext taskContext = new TaskContext();
                taskContext.setTaskName(action.getTaskName());
                taskContext.setSchedule(CronUtils.assembleIbmCronExpression(action.getSchedule()));
                taskContext.setUserTaskJndi(action.getUserTaskJndi());
                taskContext.setNumberOfRepeats(action.getNumberOfRepeats());

                Map<String, TaskParam> taskParams = new HashMap<String, TaskParam>();
                for (int i = 0; i < action.getParams().size(); i++) {
                    TaskParamModel param = action.getParams().get(i);
                    String paramValue = (param.getTaskParamValue() != null) ? param.getTaskParamValue() :
                            df.format(param.getTaskParamDateValue());
                    taskParams.put(param.getTaskParamName(),
                            new TaskParam(i, param.getTaskParamName(),
                                    TaskParamType.getTypeById(param.getTaskParamType()), paramValue));
                }
                taskContext.setParams(taskParams);

                taskManager.createTask(taskContext);
            } else {
                throw new ActionException("Задача с таким именем уже существует");
            }
        } catch (TaskSchedulingException e) {
            throw new ActionException("Ошибка создания задачи планировщика", e);
        }
        return result;
    }

    @Override
    public void undo(CreateTaskAction CreateTaskAction, CreateTaskResult CreateTaskResult, ExecutionContext executionContext) throws ActionException {
        //do nothing
    }
}
