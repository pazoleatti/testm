package com.aplana.sbrf.taxaccounting.scheduler.core.manager;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.*;
import com.aplana.sbrf.taxaccounting.scheduler.api.exception.TaskSchedulingException;
import com.aplana.sbrf.taxaccounting.scheduler.api.manager.TaskManager;
import com.aplana.sbrf.taxaccounting.scheduler.api.manager.TaskManagerLocal;
import com.aplana.sbrf.taxaccounting.scheduler.api.manager.TaskManagerRemote;
import com.aplana.sbrf.taxaccounting.scheduler.api.task.UserTask;
import com.aplana.sbrf.taxaccounting.scheduler.api.task.UserTaskRemote;
import com.aplana.sbrf.taxaccounting.scheduler.core.entity.TaskContextEntity;
import com.aplana.sbrf.taxaccounting.scheduler.core.persistence.TaskPersistenceServiceLocal;
import com.aplana.sbrf.taxaccounting.scheduler.core.service.TaskServiceLocal;
import com.aplana.sbrf.taxaccounting.scheduler.core.task.TaskExecutorRemoteHome;
import com.aplana.sbrf.taxaccounting.scheduler.core.utils.TaskUtils;
import com.ibm.websphere.scheduler.*;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.naming.Binding;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Remote(TaskManagerRemote.class)
@Local(TaskManagerLocal.class)
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class TaskManagerBean implements TaskManager {
    private static final Log LOG = LogFactory.getLog(TaskManagerBean.class);

    /** Базовый jndi-путь с которого начинается поиск задач */
    private static final String BASE_JNDI_NAME = "ejb";

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
        return createTask(taskContext, true);
    }

    private Long createTask(TaskContext taskContext, boolean newContext) throws TaskSchedulingException {
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
            taskContextEntity.setId(taskContext.getId());
            taskContextEntity.setModificationDate(new Date());
            taskContextEntity.setUserId(taskContext.getUserId());
            if (newContext){
                persistenceService.saveContext(taskContextEntity);
            } else {
                persistenceService.updateContext(taskContextEntity);
            }

            LOG.info(String.format("New task has been created. Task id: %s; Next call: %s",
                    taskStatus.getTaskId(),
                    taskStatus.getNextFireTime()));
            return Long.parseLong(taskStatus.getTaskId());
        } catch (UserCalendarPeriodInvalid e) {
            throw new TaskSchedulingException("Значение атрибута «Расписание» не соответствует требованиям формата Cron!", e);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new TaskSchedulingException("Не удалось выполнить создание задачи", e);
        }
    }

    @Override
    public void deleteTask(Long taskId) throws TaskSchedulingException {
        LOG.info(String.format("Task deleting has been started. Task id: %s", taskId));
        try {
            //Удаляем наш контекст
            persistenceService.deleteContextByTaskId(taskId);
        } catch (Exception e) {
            checkRunningTask(e);
            LOG.error(e.getLocalizedMessage(), e);
            throw new TaskSchedulingException("Не удалось выполнить удаление задачи", e);
        }

        try {
            //Удаляем задачу у планировщика если получится, т.к она все равно не будет работать без нашего контекста. Но планировщик иногда тупит и не дает удалить
            scheduler.cancel(taskId.toString(), true);
        } catch (Exception e) {
            checkRunningTask(e);
            LOG.error(e.getLocalizedMessage(), e);
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
            checkRunningTask(e);
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
            checkRunningTask(e);
            LOG.error(e.getLocalizedMessage(), e);
            throw new TaskSchedulingException("Не удалось выполнить возобновление задачи", e);
        }
    }

    @Override
    public TaskData getTaskData(Long taskId) throws TaskSchedulingException {
        LOG.info(String.format("Obtaining task data. Task id: %s", taskId));
        try {
            TaskContextEntity taskContextEntity = persistenceService.getContextByTaskId(taskId);
            TaskData taskData = getBasicTaskData(taskContextEntity);
            TaskInfo taskInfo = scheduler.getTask(taskId.toString());
            fillIBMTaskData(taskData, taskInfo);
            fillComplexTaskData(taskData, taskContextEntity);
            return taskData;
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

        for (TaskContextEntity context : contexts) {
            TaskData taskData = getBasicTaskData(context);
            try {
                TaskInfo taskInfo = scheduler.getTask(String.valueOf(context.getTaskId()));
                fillIBMTaskData(taskData, taskInfo);
                fillComplexTaskData(taskData, context);
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
                try {
                    deleteTask(taskData.getTaskId());
                } catch (TaskSchedulingException e1) {
                    LOG.error(e1.getLocalizedMessage(), e1);
                }
                taskData.setOldAndDeleted(true);
            }
            tasksData.add(taskData);
        }
        return tasksData;
    }

    @Override
    public void updateTask(Long taskId, TaskContext taskContext) throws TaskSchedulingException {
        LOG.info(String.format("Task updating has been started. Task id: %s", taskId));
        //TODO api ibm не позволяет обновлять данные задачи. Надо удалять и создавать новую
        try {
            createTask(taskContext, false);
            scheduler.cancel(taskId.toString(), true);
        } catch (Exception e) {
            checkRunningTask(e);
            LOG.error(e.getLocalizedMessage(), e);
            throw new TaskSchedulingException(e);
        }
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

    @Override
    public List<TaskJndiInfo> getTasksJndi(TAUserInfo userInfo) throws TaskSchedulingException {
        LOG.info("Obtaining all tasks jndi");
        try {
            InitialContext ic = new InitialContext();
            NamingEnumeration<Binding> tasks = ic.listBindings(BASE_JNDI_NAME);
            List<TaskJndiInfo> jndiInfo = new ArrayList<TaskJndiInfo>();
            while (tasks.hasMore()) {
                Binding binding = tasks.next();
                traverseJndiTree(binding, BASE_JNDI_NAME, ic, jndiInfo, userInfo);
            }
            return jndiInfo;
        } catch (NamingException e) {
            throw new TaskSchedulingException(e);
        }
    }

    @Override
    public boolean validateSchedule(String schedule) throws TaskSchedulingException {
        try {
            // Create an initial context
            InitialContext ctx = null;
            // Lookup and narrow the default UserCalendar home.
            UserCalendarHome defaultCalHome = null;
            try {
                ctx = new InitialContext();
                defaultCalHome = (UserCalendarHome) PortableRemoteObject.narrow(ctx.lookup(UserCalendarHome.DEFAULT_CALENDAR_JNDI_NAME), UserCalendarHome.class);
            } catch (NamingException e) {
                throw new TaskSchedulingException("NamingException", e);
            }

            // Create the default UserCalendar instance.
            UserCalendar defaultCal;
            try {
                defaultCal = defaultCalHome.create();

            } catch (CreateException e) {
                throw new TaskSchedulingException("CreateException", e);
            }

            defaultCal.validate("CRON", schedule);

        } catch (UserCalendarSpecifierInvalid userCalendarSpecifierInvalid) {
            throw new TaskSchedulingException("Не верный идентификатор календаря");
        } catch (UserCalendarPeriodInvalid userCalendarPeriodInvalid) {
            return false;
        } catch (RemoteException e) {
            throw new TaskSchedulingException("Системная ошибка");
        }

        return true;
    }

    private void checkRunningTask(Exception e) {
        Throwable rootCause = ExceptionUtils.getRootCause(e);
        //Иначе определить информацию не получается, т.к вебсфера не меняет статус задачи
        if (rootCause instanceof java.sql.SQLSyntaxErrorException && rootCause.getLocalizedMessage().contains("ORA-02049")) {
            throw new ServiceException("Сохранение изменений невозможно! Задача выполняется в данный момент.");
        }
    }

    /**
     * Обход дерева jndi элементов
     * @param item элемент дерева
     * @param jndiName составное jndi-имя элемента дерева
     * @param ic ejb-контекст
     * @param jndiInfo список найденных задач планировщика
     * @throws NamingException
     */
    private void traverseJndiTree(Binding item, String jndiName, InitialContext ic, List<TaskJndiInfo> jndiInfo, TAUserInfo userInfo) throws NamingException {
        if (item.getObject() instanceof UserTask) {
            UserTask userTask = (UserTask) PortableRemoteObject.narrow(item.getObject(), UserTask.class);
            StringBuilder jndi = new StringBuilder(jndiName)
                    .append("/")
                    .append(userTask.getTaskClassName())
                    .append("#")
                    .append(UserTaskRemote.class.getName());
            jndiInfo.add(new TaskJndiInfo(userTask.getTaskName(), jndi.toString(), userTask.getParams(userInfo)));
        }

        String newJndiName = jndiName + "/" + item.getName();
        NamingEnumeration<Binding> bindings = null;

        try {
            bindings = ic.listBindings(newJndiName);
        } catch (NamingException e) {
            //Ничего не делаем. Просто проверка на существование элемента
            return;
        }

        while (bindings.hasMore()) {
            traverseJndiTree(bindings.next(), newJndiName, ic, jndiInfo, userInfo);
        }
    }

    private TaskData getBasicTaskData(TaskContextEntity taskContextEntity) {
        //Формирование ответа
        TaskData taskData = new TaskData();
        taskData.setTaskName(taskContextEntity.getTaskName());
        taskData.setTaskId(taskContextEntity.getTaskId());
        taskData.setUserTaskJndi(taskContextEntity.getUserTaskJndi());
        taskData.setModificationDate(taskContextEntity.getModificationDate());
        taskData.setContextId(taskContextEntity.getId());
        return taskData;
    }

    /**
     * Получение данных из планировщика IBM, во время которого может произойти ошибка. В этом случае, задача считается устаревшей
     * @param taskData базовые данные задачи
     * @param taskInfo данные из планировщика ibm
     */
    private void fillIBMTaskData(TaskData taskData, TaskInfo taskInfo) {
        taskData.setTaskState(TaskState.getStateById(taskInfo.getStatus()));
        taskData.setSchedule(taskInfo.getRepeatInterval());
        taskData.setNumberOfRepeats(taskInfo.getNumberOfRepeats());
        taskData.setRepeatsLeft(taskInfo.getRepeatsLeft());
        taskData.setTimeCreated(taskInfo.getTimeCreated());
        taskData.setNextFireTime(taskInfo.getNextFireTime());
    }

    /**
     * Получение кастомных параметров задачи, при котором может произойти ошибка. В этом случае, задача считается устаревшей
     * @param taskData базовые данные задачи
     * @param taskContextEntity данные из бд с кастомными параметрами
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void fillComplexTaskData(TaskData taskData, TaskContextEntity taskContextEntity) throws IOException,
            ClassNotFoundException {
        Map<String, TaskParam> params = null;

        if (taskContextEntity.isCustomParamsExist()) {
            params = TaskUtils.deserializeParams(taskContextEntity.getSerializedParams());
        }
        taskData.setParams(params);
    }
}
