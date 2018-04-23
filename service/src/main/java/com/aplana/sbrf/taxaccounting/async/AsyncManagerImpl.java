package com.aplana.sbrf.taxaccounting.async;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskSerializationException;
import com.aplana.sbrf.taxaccounting.dao.AsyncTaskDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.utils.ApplicationInfo;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Реализация менеджера асинхронных задач на Spring и без использования ejb и jms
 * При добавлении асинхронной задачи, сохраняет ее параметры в БД в сериализованном виде, а выполнением задач занимается специализированный класс {@link AsyncTaskThreadContainer.AsyncTaskLongQueueProcessor} или {@link AsyncTaskThreadContainer.AsyncTaskShortQueueProcessor}
 *
 * @author dloshkarev
 */
@Transactional
@Component
public class AsyncManagerImpl implements AsyncManager {
    private static final Log LOG = LogFactory.getLog(AsyncManagerImpl.class);

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private ApplicationInfo applicationInfo;
    @Autowired
    private ServerInfo serverInfo;
    @Autowired
    private TransactionHelper tx;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private TAUserService userService;
    @Autowired
    private LockDataService lockDataService;
    @Autowired
    private AsyncTaskDao asyncTaskDao;

    private static final ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd/MM/yyyy HH:mm z");
        }
    };

    @Override
    public AsyncTask getAsyncTaskBean(long taskTypeId) throws AsyncTaskException {
        AsyncTaskTypeData asyncTaskType = asyncTaskDao.getTaskTypeData(taskTypeId);

        if (asyncTaskType == null) {
            throw new AsyncTaskException("Cannot find parameters for async task with id " + taskTypeId + " in database table ASYNC_TASK_TYPE");
        }

        if (asyncTaskType.getHandlerClassName().startsWith("ejb")) {
            throw new AsyncTaskException("Incorrect name for bean-executor");
        }

        AsyncTask task;
        try {
            task = applicationContext.getBean(asyncTaskType.getHandlerClassName(), AsyncTask.class);
        } catch (NoSuchBeanDefinitionException e) {
            throw new AsyncTaskException("Cannot find bean-executor for task type with id " + taskTypeId + ", from database table ASYNC_TASK_TYPE", e);
        } catch (Exception e) {
            throw new AsyncTaskException("Unexpected error during get bean-executor for task type " + taskTypeId + ", from database table ASYNC_TASK_TYPE", e);
        }
        return task;
    }

    @Override
    public AsyncTaskData getLightTaskData(long taskId) {
        return asyncTaskDao.getLightTaskData(taskId);
    }

    @Override
    public AsyncTaskData executeTask(String lockKey, AsyncTaskType taskType, TAUserInfo user, AsyncQueue queue, Map<String, Object> params) throws AsyncTaskException {
        LOG.debug("Async task creation has been started");
        LockData lockData = lockDataService.getLock(lockKey);

        if (lockData != null) {
            try {
                if (!MapUtils.isEmpty(params)) {
                    checkParams(params);
                }
                // Получение и проверка класса обработчика задачи
                AsyncTask task = getAsyncTaskBean(taskType.getAsyncTaskTypeId());
                String description = task.getDescription(user, params);
                if (queue == null) {
                    queue = task.checkTaskLimit(description, user, params);
                }
                // Сохранение в очереди асинхронных задач - запись в БД
                String priorityNode = applicationInfo.isProductionMode() ? null : serverInfo.getServerName();
                AsyncTaskData taskData = asyncTaskDao.addTask(taskType.getAsyncTaskTypeId(), user.getUser().getId(), description, queue, priorityNode, AsyncTaskGroupFactory.getTaskGroup(taskType), params);
                lockDataService.bindTask(lockKey, taskData.getId());

                LOG.info(String.format("Task with id %s was put in queue %s. Task type: %s, priority node: %s",
                        taskData.getId(), queue.name(), taskType.getId(), priorityNode));
                LOG.debug("Async task creation has been finished successfully");
                return taskData;
            } catch (Exception e) {
                LOG.error("Async task creation has been failed!", e);
                throw new AsyncTaskException(e);
            }
        } else {
            throw new AsyncTaskException("Cannot execute task. Lock doesn't exists.");
        }
    }

    @Override
    public AsyncTaskData executeTask(String lockKey, AsyncTaskType taskType, TAUserInfo user, AsyncQueue queue) throws AsyncTaskException {
        return executeTask(lockKey, taskType, user, queue, Collections.<String, Object>emptyMap());
    }

    @Override
    public AsyncTaskData executeTask(String lockKey, AsyncTaskType taskType, TAUserInfo user) throws AsyncTaskException {
        return executeTask(lockKey, taskType, user, null, Collections.<String, Object>emptyMap());
    }

    @Override
    public AsyncTaskData executeTask(String lockKey, AsyncTaskType taskType, TAUserInfo user, Map<String, Object> params) throws AsyncTaskException {
        return executeTask(lockKey, taskType, user, null, params);
    }

    @Override
    public AsyncTaskData executeTask(final String lockKey, final AsyncTaskType taskType, final TAUserInfo user, final Map<String, Object> params, final Logger logger, final boolean cancelConfirmed, final AbstractStartupAsyncTaskHandler handler) {
        tx.executeInNewTransaction(new TransactionLogic() {
            @Override
            public Object execute() {
                LOG.info(String.format("Выполнение проверок перед запуском для задачи с ключом %s", lockKey));
                if (!cancelConfirmed && handler.checkExistTasks(taskType, user, logger)) {
                    //Задачи найдены, но их отмена пока не подтверждена - запрашиваем подтверждение у пользователя
                    LOG.info(String.format("Найдены запущенные задачи, по которым требуется удалить блокировку для задачи с ключом %s", lockKey));
                    handler.postCheckProcessing();
                } else {
                    //Задачи найдены и подтверждена их отмена
                    LOG.info(String.format("Создание блокировки для задачи с ключом %s", lockKey));
                    LockData lockData = handler.lockObject(lockKey, taskType, user);
                    AsyncTaskData taskData = null;
                    if (lockData == null) {
                        //Блокировка успешно установлена
                        try {
                            //Остановка найденных задач + выполнение специфичной бизнес-логики
                            handler.interruptTasks(taskType, user);

                            //Постановка новой задачи в очередь
                            LOG.info(String.format("Постановка в очередь задачи с ключом %s", lockKey));
                            taskData = executeTask(lockKey, taskType, user, params);

                            //Выполнение пост-обработки
                            handler.postTaskScheduling(taskData, logger);
                            return taskData;
                        } catch (Exception e) {
                            if (taskData != null) {
                                finishTask(taskData.getId());
                            } else {
                                lockDataService.unlock(lockKey, user.getUser().getId(), true);
                            }
                            int i = ExceptionUtils.indexOfThrowable(e, ServiceLoggerException.class);
                            if (i != -1) {
                                throw (ServiceLoggerException) ExceptionUtils.getThrowableList(e).get(i);
                            }
                            throw new ServiceException(e.getMessage(), e);
                        }
                    } else {
                        //Блокировка не была установлена, т.к этот объект был заблокирован ранее - добавляем пользователя в подписчики на задачу
                        LOG.info(String.format("Уже существует блокировка задачи с ключом %s", lockKey));
                        addUserWaitingForTask(lockData.getTaskId(), user.getUser().getId());
                        logger.info(String.format(AsyncTask.LOCK_INFO_MSG,
                                sdf.get().format(lockData.getDateLock()),
                                userService.getUser(lockData.getUserId()).getName()));
                    }
                }
                return null;
            }
        });

        return null;
    }

    @Override
    public Pair<Boolean, String> restartTask(String lockKey, TAUserInfo user, boolean force, Logger logger) {
        LockData lockData = lockDataService.getLock(lockKey);
        if (lockData != null) {
            AsyncTaskData taskData = asyncTaskDao.getLightTaskData(lockData.getTaskId());
            if (taskData != null) {
                if (taskData.getUserId() == user.getUser().getId()) {
                    if (force) {
                        //Удаляем старую задачу, оправляем оповещения подписавщимся пользователям
                        interruptTask(lockData.getTaskId(), user, TaskInterruptCause.RESTART_TASK);
                    } else {
                        //Вызов диалога
                        String restartMsg = AsyncTaskState.IN_QUEUE.equals(taskData.getState()) ?
                                String.format(AsyncTask.CANCEL_MSG, taskData.getDescription()) :
                                String.format(AsyncTask.RESTART_MSG, taskData.getDescription());
                        return new Pair<Boolean, String>(true, restartMsg);
                    }
                } else {
                    //Добавляем пользователя  в подписчики на задачу
                    addUserWaitingForTask(taskData.getId(), user.getUser().getId());
                    logger.info(String.format(AsyncTask.LOCK_INFO_MSG,
                            sdf.get().format(taskData.getCreateDate()),
                            userService.getUser(taskData.getUserId()).getName()));
                    return new Pair<Boolean, String>(false, null);
                }
            }
        }
        return null;
    }

    public void interruptTask(final long taskId, final TAUserInfo user, final TaskInterruptCause cause) {
        final AsyncTaskData taskData = asyncTaskDao.getLightTaskData(taskId);
        interruptTask(taskData, user, cause);
    }

    @Override
    public void interruptTask(final AsyncTaskData taskData, final TAUserInfo user, final TaskInterruptCause cause) {
        if (taskData != null) {
            LOG.info(String.format("Останавливается асинхронная задача с id %s", taskData.getId()));
            tx.executeInNewTransaction(new TransactionLogic() {
                                           @Override
                                           public Object execute() {
                                               try {
                                                   if (taskData.getState() != AsyncTaskState.CANCELLED) {
                                                       List<Integer> waitingUsers = getUsersWaitingForTask(taskData.getId());
                                                       if (!waitingUsers.contains(user.getUser().getId())) {
                                                           waitingUsers.add(user.getUser().getId());
                                                       }
                                                       String msg = String.format(AsyncTask.CANCEL_TASK, user.getUser().getName(), taskData.getDescription(), cause.toString());
                                                       List<Notification> notifications = new ArrayList<Notification>();
                                                       //Создаем оповещение для каждого пользователя из списка
                                                       if (!waitingUsers.isEmpty()) {
                                                           for (Integer waitingUser : waitingUsers) {
                                                               Notification notification = new Notification();
                                                               notification.setUserId(waitingUser);
                                                               notification.setCreateDate(new Date());
                                                               notification.setText(msg);
                                                               notifications.add(notification);
                                                           }
                                                           notificationService.create(notifications);
                                                       }
                                                       asyncTaskDao.cancelTask(taskData.getId());
                                                   } else {
                                                       asyncTaskDao.finishTask(taskData.getId());
                                                   }
                                               } catch (Exception e) {
                                                   throw new ServiceException("Не удалось прервать задачу", e);
                                               }
                                               return null;
                                           }
                                       }
            );
        }
    }

    @Override
    public void interruptTask(String lockKey, TAUserInfo user, TaskInterruptCause cause) {
        LockData lockData = lockDataService.getLock(lockKey);
        if (lockData != null) {
            final AsyncTaskData taskData = asyncTaskDao.getLightTaskData(lockData.getTaskId());
            interruptTask(taskData, user, cause);
        }
    }

    @Override
    public void finishTask(final long taskId) {
        tx.executeInNewTransaction(new TransactionLogic() {
            @Override
            public Object execute() {
                try {
                    synchronized (AsyncManagerImpl.class) {
                        asyncTaskDao.finishTask(taskId);
                        lockDataService.unlockAllByTask(taskId);
                    }
                } catch (Exception e) {
                    throw new ServiceException("Не удалось завершить асинхронную задачу", e);
                }
                return null;
            }
        });
    }

    @Override
    public void interruptAllTasks(List<Long> taskIds, TAUserInfo user, TaskInterruptCause cause) {
        for (Long id : taskIds) {
            interruptTask(id, user, cause);
        }
    }

    @Override
    public void updateState(final long taskId, final AsyncTaskState state) {
        tx.executeInNewTransaction(new TransactionLogic() {
            @Override
            public Object execute() {
                try {
                    synchronized (AsyncManagerImpl.class) {
                        asyncTaskDao.updateState(taskId, state);
                    }
                } catch (Exception e) {
                    throw new ServiceException("Не удалось обновить статус асинхронной задачи", e);
                }
                return null;
            }
        });
    }

    @Override
    public AsyncTaskData reserveTask(String node, String priorityNode, int timeout, AsyncQueue balancingVariants, int maxTasksPerNode) {
        int rowsUpdated = asyncTaskDao.lockTask(node, priorityNode, timeout, balancingVariants, maxTasksPerNode);
        if (rowsUpdated != 0) {
            LOG.debug(String.format("Node '%s' reserve tasks: %s", node, rowsUpdated));
        }
        if (rowsUpdated > 1) {
            throw new IllegalArgumentException("Incorrect tasks reserved per node!");
        }
        if (rowsUpdated == 1) {
            return asyncTaskDao.getLockedTask(node, balancingVariants);
        }
        return null;
    }

    @Override
    public void addUserWaitingForTask(long taskId, int userId) {
        if (asyncTaskDao.isTaskExists(taskId)) {
            if (!asyncTaskDao.getUsersWaitingForTask(taskId).contains(userId)) {
                asyncTaskDao.addUserWaitingForTask(taskId, userId);
            }
        } else {
            LOG.warn(String.format("Cannot add subscriber with id = %s to task with id = %s. Cause: task doesn't exists", userId, taskId));
        }
    }

    @Override
    public List<Integer> getUsersWaitingForTask(long taskId) {
        return asyncTaskDao.getUsersWaitingForTask(taskId);
    }

    @Override
    public PagingResult<AsyncTaskDTO> getTasks(String filter, PagingParams pagingParams) {
        return asyncTaskDao.getTasks(filter, pagingParams);
    }

    @Override
    public void releaseNodeTasks() {
        String currentNode = serverInfo.getServerName();
        LOG.info("Освобождение задач для узла: " + currentNode);
        if (applicationInfo.isProductionMode()) {
            asyncTaskDao.releaseNodeTasks(currentNode);
        } else {
            List<Long> taskIds = asyncTaskDao.getTasksByPriorityNode(currentNode);
            for (Long id : taskIds) {
                finishTask(id);
            }
        }
    }

    @Override
    public boolean isTaskActive(long taskId) {
        return asyncTaskDao.isTaskActive(taskId);
    }

    /**
     * Проверка параметров. Все они должны сериализоваться
     */
    private void checkParams(Map<String, Object> params) throws AsyncTaskSerializationException {
        for (Map.Entry<String, Object> param : params.entrySet()) {
            //Все параметры должны быть сериализуемы
            if (!Serializable.class.isAssignableFrom(param.getValue().getClass())) {
                throw new AsyncTaskSerializationException("Attribute \"" + param.getKey() + "\" doesn't support serialization!");
            }
        }
    }

    /**
     * Фабрика группы асинхронной задачи по типу асинхронной задачи
     */
    public static class AsyncTaskGroupFactory {
        public static AsyncTaskGroup getTaskGroup(AsyncTaskType taskType) {
            switch (taskType) {
                case IDENTIFY_PERSON:
                case IMPORT_REF_BOOK_XML:
                    return AsyncTaskGroup.REF_BOOK_PERSON;
                default:
                    return null;
            }
        }
    }
}
