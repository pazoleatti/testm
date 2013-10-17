package com.aplana.sbrf.taxaccounting.scheduler.core.manager;

import com.aplana.sbrf.taxaccounting.scheduler.api.manager.TaskManager;
import com.aplana.sbrf.taxaccounting.scheduler.core.task.TaskExecutorRemoteHome;
import com.aplana.sbrf.taxaccounting.scheduler.core.utils.TaskUtils;
import com.ibm.websphere.scheduler.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskContext;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskData;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskParam;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskState;
import com.aplana.sbrf.taxaccounting.scheduler.api.exception.TaskSchedulingException;
import com.aplana.sbrf.taxaccounting.scheduler.api.manager.TaskManagerLocal;
import com.aplana.sbrf.taxaccounting.scheduler.api.manager.TaskManagerRemote;
import com.aplana.sbrf.taxaccounting.scheduler.core.entity.TaskContextEntity;
import com.aplana.sbrf.taxaccounting.scheduler.core.persistence.TaskPersistenceServiceLocal;
import com.aplana.sbrf.taxaccounting.scheduler.core.service.TaskServiceLocal;

import javax.annotation.Resource;
import javax.ejb.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Remote(TaskManagerRemote.class)
@Local(TaskManagerLocal.class)
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class TaskManagerBean implements TaskManager {
    private static final Log LOG = LogFactory.getLog(TaskManagerBean.class);

    @Resource(name = "sched/TaskScheduler")
    private Scheduler scheduler;

    @EJB
    private TaskExecutorRemoteHome taskExecutor;

    @EJB
    private TaskPersistenceServiceLocal persistenceService;

    @EJB
    private TaskServiceLocal taskService;

    @Override
    public Long createTask(TaskContext taskContext) throws TaskSchedulingException {
        LOG.info("New task creation has been started");
        try {
            //Планирование задачи в IBM Scheduler
            BeanTaskInfo taskInfo = (BeanTaskInfo) scheduler.createTaskInfo(BeanTaskInfo.class);
            taskInfo.setTaskHandler(taskExecutor);
            taskInfo.setUserCalendar(null, "CRON");
            taskInfo.setNumberOfRepeats(taskContext.getNumberOfRepeats());
            taskInfo.setStartTimeInterval(taskContext.getSchedule());
            taskInfo.setRepeatInterval(taskContext.getSchedule());
            taskInfo.setName(taskContext.getTaskName());
            //Параметр для длительных задач
            //taskInfo.setQOS(com.ibm.websphere.scheduler.TaskData.QOS_ATLEASTONCE);

            TaskStatus taskStatus = scheduler.create(taskInfo);

            //Сериализация контекста и его сохранение в бд
            TaskContextEntity taskContextEntity = new TaskContextEntity();
            if (!taskContext.getParams().isEmpty()) {
                byte[] contextBytes = TaskUtils.serializeParams(taskContext.getParams());
                taskContextEntity.setSerializedParams(contextBytes);
                taskContextEntity.setCustomParamsExist(true);
            } else {
                taskContextEntity.setCustomParamsExist(false);
            }
            taskContextEntity.setTaskId(Long.parseLong(taskStatus.getTaskId()));
            taskContextEntity.setTaskName(taskContext.getTaskName());
            taskContextEntity.setUserTaskJndi(taskContext.getUserTaskJndi());
            persistenceService.saveContext(taskContextEntity);

            LOG.info(String.format("New task has been created. Task id: %s; Next call: %s",
                    taskStatus.getTaskId(),
                    taskStatus.getNextFireTime()));
            return Long.parseLong(taskStatus.getTaskId());
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new TaskSchedulingException("Не удалось выполнить создание задачи", e);
        }
    }

    @Override
    public void deleteTask(Long taskId) throws TaskSchedulingException {
        LOG.info(String.format("Task deleting has been started. Task id: %s", taskId));
        try {
            scheduler.cancel(taskId.toString(), true);
            persistenceService.deleteContextByTaskId(taskId);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new TaskSchedulingException("Не удалось выполнить удаление задачи", e);
        }
    }

    @Override
    public void startTask(Long taskId) throws TaskSchedulingException {
        LOG.info(String.format("Starting task in progress. Task id: %s", taskId));
        taskService.startTaskById(taskId);
    }

    @Override
    public void stopTask(Long taskId) throws TaskSchedulingException {
        LOG.info(String.format("Stopping task in progress. Task id: %s", taskId));
        try {
            scheduler.suspend(taskId.toString());
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new TaskSchedulingException("Не удалось выполнить остановку задачи", e);
        }
    }

    @Override
    public void resumeTask(Long taskId) throws TaskSchedulingException {
        LOG.info(String.format("Resuming task in progress. Task id: %s", taskId));
        try {
            scheduler.resume(taskId.toString());
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new TaskSchedulingException("Не удалось выполнить возобновление задачи", e);
        }
    }

    @Override
    public TaskData getTaskData(Long taskId) throws TaskSchedulingException {
        LOG.info(String.format("Obtaining task data. Task id: %s", taskId));
        try {
            TaskInfo taskInfo = scheduler.getTask(taskId.toString());
            TaskContextEntity taskContextEntity = persistenceService.getContextByTaskId(taskId);
            return getFullTaskData(taskInfo, taskContextEntity);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new TaskSchedulingException(e);
        }
    }

    @Override
    public List<TaskData> getAllTasksData() throws TaskSchedulingException {
        LOG.info("Obtaining all tasks data");
        List<TaskData> tasksData = new ArrayList<TaskData>();
        List<TaskContextEntity> contexts = persistenceService.getAllContexts();

        try {
            for (TaskContextEntity context : contexts) {
                TaskInfo taskInfo = scheduler.getTask(String.valueOf(context.getTaskId()));
                tasksData.add(getFullTaskData(taskInfo, context));
            }
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new TaskSchedulingException("Не удалось получить список задач", e);
        }
        return tasksData;
    }

    @Override
    public void updateTask(Long taskId, TaskContext taskContext) throws TaskSchedulingException {
        LOG.info(String.format("Task updating has been started. Task id: %s", taskId));
        //TODO api ibm не позволяет обновлять данные задачи. Надо удалять и создавать новую
        /*try {
            scheduler.cancel(taskId.toString(), true);
            persistenceService.deleteContextByTaskId(taskId);
            createTask(taskContext);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new TaskSchedulingException(e);
        }*/
    }

    @Override
    public Boolean isTaskExist(String taskName) throws TaskSchedulingException {
        LOG.info(String.format("Checking the task existence. Task name: %s", taskName));
        try {
            return scheduler.findTasksByName(taskName).hasNext();
        } catch (SchedulerNotAvailableException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new TaskSchedulingException("Не удалось проверить существование задачи", e);
        }
    }

    @Override
    public Boolean isTaskExist(Long taskId) throws TaskSchedulingException {
        LOG.info(String.format("Checking the task existence. Task id: %s", taskId));
        try {
            return scheduler.getTask(taskId.toString()) != null;
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new TaskSchedulingException("Не удалось проверить существование задачи", e);
        }
    }

    private TaskData getFullTaskData(TaskInfo taskInfo, TaskContextEntity taskContextEntity) throws IOException,
            ClassNotFoundException {

        Map<String, TaskParam> params = null;

        if (taskContextEntity.isCustomParamsExist()) {
            params = TaskUtils.deserializeParams(taskContextEntity.getSerializedParams());
        }

        //Формирование ответа
        TaskData taskData = new TaskData();
        taskData.setTaskId(taskContextEntity.getTaskId());
        taskData.setTaskName(taskInfo.getName());
        taskData.setTaskState(TaskState.getStateById(taskInfo.getStatus()));
        taskData.setSchedule(taskInfo.getRepeatInterval());
        taskData.setUserTaskJndi(taskContextEntity.getUserTaskJndi());
        taskData.setNumberOfRepeats(taskInfo.getNumberOfRepeats());
        taskData.setRepeatsLeft(taskInfo.getRepeatsLeft());
        taskData.setTimeCreated(taskInfo.getTimeCreated());
        taskData.setNextFireTime(taskInfo.getNextFireTime());
        taskData.setParams(params);
        return taskData;
    }
}
