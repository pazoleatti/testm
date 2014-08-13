package com.aplana.sbrf.taxaccounting.web.module.scheduler.server;

import com.aplana.sbrf.taxaccounting.model.TaskSearchResultItem;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskData;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskState;
import com.aplana.sbrf.taxaccounting.scheduler.api.exception.TaskSchedulingException;
import com.aplana.sbrf.taxaccounting.scheduler.api.manager.TaskManager;
import com.aplana.sbrf.taxaccounting.web.module.scheduler.shared.GetTaskListAction;
import com.aplana.sbrf.taxaccounting.web.module.scheduler.shared.GetTaskListResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Получение списка задач планировщика
 * @author dloshkarev
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
public class GetTaskListHandler extends AbstractActionHandler<GetTaskListAction, GetTaskListResult> {

    private static final String DATE_FORMAT = "dd-MM-yyyy, HH:mm";

    @Autowired
    TaskManager taskManager;

    public GetTaskListHandler() {
        super(GetTaskListAction.class);
    }

    @Override
    public GetTaskListResult execute(GetTaskListAction getTaskListAction, ExecutionContext executionContext) throws ActionException {
/*        GetTaskListResult result = new GetTaskListResult();
        List<TaskSearchResultItem> records = new ArrayList<TaskSearchResultItem>();
        TaskSearchResultItem item = new TaskSearchResultItem();
        item.setId((long) 1);
        item.setName("Test");
        item.setState("State");
        item.setNumberOfRepeats(1);
        item.setRepeatsLeft(2);
        item.setTimeChanged("11.11.2014");
        item.setNextFireTime("11.11.2015");
        records.add(item);
        result.setTasks(records);
        return result;*/
        try {
            SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
            GetTaskListResult result = new GetTaskListResult();
            List<TaskSearchResultItem> records = new ArrayList<TaskSearchResultItem>();

            List<TaskData> tasks = taskManager.getAllTasksData();
            for (TaskData task : tasks) {
                TaskSearchResultItem item = new TaskSearchResultItem();
                item.setId(task.getTaskId());
                item.setName(task.getTaskName());
                item.setState(task.getTaskState().getName());
                item.setModificationDate(df.format(task.getModificationDate()));
                item.setNextFireTime(task.getTaskState() != TaskState.SUSPENDED ? df.format(task.getNextFireTime()) : "");
                item.setContextId(task.getContextId());

                records.add(item);
            }
            result.setTasks(records);
            return result;
        } catch (TaskSchedulingException e) {
            throw new ActionException("Ошибка получения списка задач планировщика", e);
        }
    }

    @Override
    public void undo(GetTaskListAction getTaskListAction, GetTaskListResult getTaskListResult, ExecutionContext executionContext) throws ActionException {
        //Do nothing
    }
}
