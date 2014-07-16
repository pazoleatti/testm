package com.aplana.sbrf.taxaccounting.web.module.scheduler.server;

import com.aplana.sbrf.taxaccounting.model.TaskParamModel;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskContext;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskData;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskParam;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskParamType;
import com.aplana.sbrf.taxaccounting.scheduler.api.exception.TaskSchedulingException;
import com.aplana.sbrf.taxaccounting.scheduler.api.manager.TaskManager;
import com.aplana.sbrf.taxaccounting.scheduler.api.utils.CronUtils;
import com.aplana.sbrf.taxaccounting.web.module.scheduler.shared.CreateTaskAction;
import com.aplana.sbrf.taxaccounting.web.module.scheduler.shared.CreateTaskResult;
import com.aplana.sbrf.taxaccounting.web.module.scheduler.shared.UpdateTaskAction;
import com.aplana.sbrf.taxaccounting.web.module.scheduler.shared.UpdateTaskResult;
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
public class UpdateTaskHandler extends AbstractActionHandler<UpdateTaskAction, UpdateTaskResult> {

    @Autowired
    TaskManager taskManager;

    public UpdateTaskHandler() {
        super(UpdateTaskAction.class);
    }

    @Override
    public UpdateTaskResult execute(UpdateTaskAction action, ExecutionContext executionContext) throws ActionException {
        SimpleDateFormat df = new SimpleDateFormat(TaskParam.DATE_FORMAT);
        UpdateTaskResult result = new UpdateTaskResult();
        try {
            // нати в кеше
            TaskData taskData = taskManager.getTaskData(action.getTaskId());
            if (!taskData.getTaskName().equals(action.getTaskName()) && taskManager.isTaskExist(action.getTaskName())){
                throw new ActionException("Задача с таким именем уже существует");
            } else {
                TaskContext taskContext = new TaskContext();
                taskContext.setTaskName(action.getTaskName());
                taskContext.setSchedule(CronUtils.assembleIbmCronExpression(action.getSchedule()));
                taskContext.setUserTaskJndi(action.getUserTaskJndi());
                taskContext.setNumberOfRepeats(action.getNumberOfRepeats());
                taskContext.setId(action.getContextId());

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
            }
        } catch (TaskSchedulingException e) {
            throw new ActionException("Ошибка создания задачи планировщика", e);
        }

        return result;
    }

    @Override
    public void undo(UpdateTaskAction updateTaskAction, UpdateTaskResult updateTaskResult, ExecutionContext executionContext) throws ActionException {

    }


}
