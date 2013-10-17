package com.aplana.sbrf.taxaccounting.scheduler.core.manager;

import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskContext;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskData;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskParam;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskState;
import com.aplana.sbrf.taxaccounting.scheduler.api.exception.TaskSchedulingException;
import com.aplana.sbrf.taxaccounting.scheduler.api.manager.TaskManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * Заглушка для управления планировщиком задач в dev-моде
 * @author dloshkarev
 */
public class TaskManagerMock implements TaskManager {
    private static final Log LOG = LogFactory.getLog(TaskManagerMock.class);

    private List<TaskData> tasks;

    public TaskManagerMock() {
        tasks = new ArrayList<TaskData>();
        TaskData task = new TaskData();
        task.setTaskId(0L);
        task.setTaskName("Test");
        task.setTaskState(TaskState.SCHEDULED);
        task.setSchedule("5 * * * *");
        task.setNumberOfRepeats(-1);
        task.setRepeatsLeft(1);
        task.setTimeCreated(new Date());
        task.setNextFireTime(new Date());
        task.setUserTaskJndi("ejb/taxaccounting/scheduler-task.jar/SimpleUserTask#com.aplana.sbrf.taxaccounting.scheduler.api.task.UserTaskRemote");
        task.setParams(new HashMap<String, TaskParam>(0));

        TaskData task2 = new TaskData();
        task2.setTaskId(1L);
        task2.setTaskName("Test2");
        task2.setTaskState(TaskState.SCHEDULED);
        task2.setSchedule("5 * * * *");
        task2.setNumberOfRepeats(-1);
        task2.setRepeatsLeft(1);
        task2.setTimeCreated(new Date());
        task2.setNextFireTime(new Date());
        task2.setUserTaskJndi("ejb/scheduler-ear/scheduler-task.jar/SimpleUserTask#ru.aplana.scheduler.api.task.UserTaskRemote");
        task2.setParams(new HashMap<String, TaskParam>(0));

        tasks.add(task);
        tasks.add(task2);
    }

    @Override
    public Long createTask(TaskContext taskContext) throws TaskSchedulingException {
        LOG.info("Mock: New task creation has been started");
        TaskData task = new TaskData();
        task.setTaskId(new Date().getTime());
        task.setTaskName(taskContext.getTaskName());
        task.setTaskState(TaskState.SCHEDULED);
        task.setSchedule(taskContext.getSchedule());
        task.setNumberOfRepeats(taskContext.getNumberOfRepeats());
        task.setRepeatsLeft(0);
        task.setTimeCreated(new Date());
        task.setNextFireTime(new Date());
        task.setUserTaskJndi(taskContext.getUserTaskJndi());
        task.setParams(taskContext.getParams());

        LOG.info("new task: "+task);
        tasks.add(task);
        return task.getTaskId();
    }

    @Override
    public void deleteTask(Long taskId) throws TaskSchedulingException {
        LOG.info(String.format("Mock: Task deleting has been started. Task id: %s", taskId));
        for (int i=0; i < tasks.size(); i++) {
            TaskData task = tasks.get(i);
            if (task.getTaskId().equals(taskId)) {
                tasks.remove(i);
            }
        }
    }

    @Override
    public void startTask(Long taskId) throws TaskSchedulingException {
        LOG.info(String.format("Mock: Starting task in progress. Task id: %s", taskId));
    }

    @Override
    public void stopTask(Long taskId) throws TaskSchedulingException {
        LOG.info(String.format("Mock: Stopping task in progress. Task id: %s", taskId));
        for (int i=0; i < tasks.size(); i++) {
            TaskData task = tasks.get(i);
            if (task.getTaskId().equals(taskId)) {
                task.setTaskState(TaskState.SUSPENDED);
                tasks.set(i, task);
            }
        }
    }

    @Override
    public void resumeTask(Long taskId) throws TaskSchedulingException {
        LOG.info(String.format("Mock: Resuming task in progress. Task id: %s", taskId));
        for (int i=0; i < tasks.size(); i++) {
            TaskData task = tasks.get(i);
            if (task.getTaskId().equals(taskId)) {
                task.setTaskState(TaskState.SCHEDULED);
                tasks.set(i, task);
            }
        }
    }

    @Override
    public TaskData getTaskData(Long taskId) throws TaskSchedulingException {
        LOG.info(String.format("Mock: Obtaining task data. Task id: %s", taskId));
        for (TaskData task : tasks) {
            if (task.getTaskId().equals(taskId)) {
                return task;
            }
        }
        return null;
    }

    @Override
    public List<TaskData> getAllTasksData() throws TaskSchedulingException {
        LOG.info("Mock: Obtaining all tasks data");
        return tasks;
    }

    @Override
    public void updateTask(Long taskId, TaskContext taskContext) throws TaskSchedulingException {
        LOG.info(String.format("Mock: Task updating has been started. Task id: %s", taskId));
    }

    @Override
    public Boolean isTaskExist(String taskName) throws TaskSchedulingException {
        LOG.info(String.format("Mock: Checking the task existence. Task name: %s", taskName));
        for (TaskData task : tasks) {
            if (task.getTaskName().equals(taskName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Boolean isTaskExist(Long taskId) throws TaskSchedulingException {
        LOG.info(String.format("Mock: Checking the task existence. Task id: %s", taskId));
        for (TaskData task : tasks) {
            if (task.getTaskId().equals(taskId)) {
                return true;
            }
        }
        return false;
    }
}
