package com.aplana.sbrf.taxaccounting.web.module.scheduler.server;

import com.aplana.sbrf.taxaccounting.model.TaskSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTaskData;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.SchedulerTaskService;
import com.aplana.sbrf.taxaccounting.service.scheduler.SchedulerService;
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
import java.util.Date;
import java.util.List;

/**
 * Получение списка задач планировщика
 *
 * @author dloshkarev
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
public class GetTaskListHandler extends AbstractActionHandler<GetTaskListAction, GetTaskListResult> {

    private static final String DATE_FORMAT = "dd-MM-yyyy, HH:mm:ss";

    @Autowired
    private LogEntryService logEntryService;
    @Autowired
    private SchedulerTaskService schedulerTaskService;
    @Autowired
    private SchedulerService schedulerService;

    public GetTaskListHandler() {
        super(GetTaskListAction.class);
    }

    @Override
    public GetTaskListResult execute(GetTaskListAction getTaskListAction, ExecutionContext executionContext) throws ActionException {
        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
        GetTaskListResult result = new GetTaskListResult();
        List<TaskSearchResultItem> records = new ArrayList<TaskSearchResultItem>();
        Logger logger = new Logger();

        List<SchedulerTaskData> tasks = schedulerTaskService.fetchAll();
        for (SchedulerTaskData task : tasks) {
            TaskSearchResultItem item = new TaskSearchResultItem();
            item.setId(task.getTask().getSchedulerTaskId());
            item.setName(task.getTaskName());
            item.setSchedule(task.getSchedule());
            item.setState(task.getSchedule() != null ? (task.isActive() ? "Активна" : "Остановлена") : "Не задано расписание");
            item.setModificationDate(df.format(task.getModificationDate()));
            item.setLastFireTime(task.getLastFireDate() != null ? df.format(task.getLastFireDate()) : "");
            Date nextFireTime = schedulerService.nextExecutionTime(task.getTask().name());
            item.setNextFireTime(nextFireTime != null ? df.format(nextFireTime) : "");
            item.setContextId(task.getTask().getSchedulerTaskId());
            records.add(item);
        }
        result.setTasks(records);
        if (logger.containsLevel(LogLevel.ERROR)) {
            result.setUuid(logEntryService.save(logger.getEntries()));
        }
        return result;
    }

    @Override
    public void undo(GetTaskListAction getTaskListAction, GetTaskListResult getTaskListResult, ExecutionContext executionContext) throws ActionException {
        //Do nothing
    }
}
