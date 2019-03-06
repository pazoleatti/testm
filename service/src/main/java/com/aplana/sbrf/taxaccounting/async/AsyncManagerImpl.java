package com.aplana.sbrf.taxaccounting.async;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskSerializationException;
import com.aplana.sbrf.taxaccounting.dao.AsyncTaskDao;
import com.aplana.sbrf.taxaccounting.dao.AsyncTaskTypeDao;
import com.aplana.sbrf.taxaccounting.model.AsyncQueue;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskDTO;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskData;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskState;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskType;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskTypeData;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.Notification;
import com.aplana.sbrf.taxaccounting.model.OperationType;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.TaskInterruptCause;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.DepartmentReportPeriodService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.LockDataService;
import com.aplana.sbrf.taxaccounting.service.NotificationService;
import com.aplana.sbrf.taxaccounting.service.ServerInfo;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.service.TransactionHelper;
import com.aplana.sbrf.taxaccounting.service.TransactionLogic;
import com.aplana.sbrf.taxaccounting.service.component.lock.locker.DeclarationLocker;
import com.aplana.sbrf.taxaccounting.service.component.operation.AsyncTaskDescriptor;
import com.aplana.sbrf.taxaccounting.utils.ApplicationInfo;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.aplana.sbrf.taxaccounting.script.service.util.ScriptUtils.isEmpty;

/**
 * Реализация менеджера асинхронных задач на Spring
 * При добавлении асинхронной задачи, сохраняет ее параметры в БД в сериализованном виде, а выполнением задач занимается специализированный класс {@link AsyncTaskThreadContainer}
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
    private DeclarationDataService declarationDataService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private DepartmentReportPeriodService departmentReportPeriodService;
    @Autowired
    private AsyncTaskDao asyncTaskDao;
    @Autowired
    private AsyncTaskTypeDao asyncTaskTypeDao;
    @Autowired
    private DeclarationLocker declarationLocker;
    @Autowired
    private AsyncTaskDescriptor asyncTaskDescriptor;

    private static final ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd/MM/yyyy HH:mm z");
        }
    };

    @Override
    public AsyncTask getAsyncTaskBean(long taskTypeId) throws AsyncTaskException {
        AsyncTaskTypeData asyncTaskType = asyncTaskTypeDao.findById(taskTypeId);

        if (asyncTaskType == null) {
            throw new AsyncTaskException("Cannot find parameters for async task with id " + taskTypeId + " in database table ASYNC_TASK_TYPE");
        }

        if (asyncTaskType.getHandlerBean().startsWith("ejb")) {
            throw new AsyncTaskException("Incorrect name for bean-executor");
        }

        AsyncTask task;
        try {
            task = applicationContext.getBean(asyncTaskType.getHandlerBean(), AsyncTask.class);
        } catch (NoSuchBeanDefinitionException e) {
            throw new AsyncTaskException("Cannot find bean-executor for task type with id " + taskTypeId + ", from database table ASYNC_TASK_TYPE", e);
        } catch (Exception e) {
            throw new AsyncTaskException("Unexpected error during get bean-executor for task type " + taskTypeId + ", from database table ASYNC_TASK_TYPE", e);
        }
        return task;
    }

    @Override
    public AsyncTaskData getLightTaskData(long taskId) {
        return asyncTaskDao.findByIdLight(taskId);
    }

    @Override
    public AsyncTaskData executeTask(String lockKey, AsyncTaskType taskType, TAUserInfo user, AsyncQueue queue, Map<String, Object> params) throws AsyncTaskException {
        LOG.info(String.format("AsyncManagerImpl.executeTask by %s. lockKey: %s; taskType: %s; queue: %s; params: %s",
                user, lockKey, taskType, queue, params));
        LockData lockData = lockDataService.findLock(lockKey);

        if (lockData != null) {
            try {
                if (!MapUtils.isEmpty(params)) {
                    checkParams(params);
                }
                // Получение и проверка класса обработчика задачи
                AsyncTask task = getAsyncTaskBean(taskType.getAsyncTaskTypeId());
                String description = task.createDescription(user, params);
                if (queue == null) {
                    queue = task.defineTaskLimit(description, user, params);
                }
                // Сохранение в очереди асинхронных задач - запись в БД
                String priorityNode = applicationInfo.isProductionMode() ? null : serverInfo.getServerName();
                AsyncTaskData taskData = asyncTaskDao.create(taskType.getAsyncTaskTypeId(), user.getUser().getId(), description, queue, priorityNode, params);
                lockDataService.bindTask(lockKey, taskData.getId());

                LOG.info(String.format("Task with id %s was put in queue %s. Task type: %s, priority node: %s",
                        taskData.getId(), queue.name(), taskType.getId(), priorityNode));
                LOG.debug("Async task creation has been finished successfully");
                return taskData;
            } catch (Exception e) {
                LOG.error("Async task creation has been failed!", e);
                throw new AsyncTaskException(e.getMessage(), e);
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
        LOG.info(String.format("AsyncManagerImpl.executeTask by %s. lockKey: %s; taskType: %s; cancelConfirmed: %s; params: %s", user, lockKey, taskType, cancelConfirmed, params));
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
                            handler.afterTaskCreated(taskData, logger);
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
    public synchronized Boolean createTask(final OperationType operationType, final String operationObjectDescription, final TAUserInfo user, final Map<String, Object> params, final Logger logger) {
        LOG.info(String.format("AsyncManagerImpl.executeTask by %s. taskType: %s; params: %s", user, operationType, params));
        return tx.executeInNewTransaction(new TransactionLogic<Boolean>() {
            @Override
            public Boolean execute() {
                List<LockData> lockDataList = null;
                AsyncTask task = null;
                AsyncTaskData taskData = null;
                List<String> keys = new ArrayList<>();
                String description = asyncTaskDescriptor.createDescription(params, operationType);
                try {
                    task = getAsyncTaskBean(operationType.getAsyncTaskTypeId());
                    if (task instanceof AsyncTaskExecutePossibilityVerifier) {
                        AsyncTaskExecutePossibilityVerifier verifier = (AsyncTaskExecutePossibilityVerifier) task;
                        if (!verifier.canExecuteByLimit()) {
                            logger.error(verifier.createExecuteByLimitErrorMessage());
                            return false;
                        }
                    }
                    if (!MapUtils.isEmpty(params)) {
                        checkParams(params);
                    }
                    lockDataList = checkAndCreateLocks(operationType, params, logger, user);
                    if (lockDataList != null && !lockDataList.isEmpty()) {
                        for (LockData lockData : lockDataList) {
                            keys.add(lockData.getKey());
                        }
                        //Постановка новой задачи в очередь
                        AsyncQueue queue = task.defineTaskLimit(description, user, params);

                        // Сохранение в очереди асинхронных задач - запись в БД
                        String priorityNode = applicationInfo.isProductionMode() ? null : serverInfo.getServerName();
                        AsyncTaskType asyncTaskType = AsyncTaskType.getByAsyncTaskTypeId(operationType.getAsyncTaskTypeId());
                        taskData = asyncTaskDao.create(operationType.getAsyncTaskTypeId(), user.getUser().getId(), description, queue, priorityNode, params);

                        lockDataService.bindTaskToMultiKeys(keys, taskData.getId());
                        logger.info("Задача %s поставлена в очередь на исполнение", description);
                        LOG.info(String.format("Task with id %s was put in queue %s. Task type: %s, priority node: %s",
                                taskData.getId(), queue.name(), asyncTaskType.getId(), priorityNode));
                        return true;
                    }
                } catch (Exception e) {
                    LOG.error("Async task creation has been failed!", e);
                    if (e instanceof ServiceException && !isEmpty(e.getMessage())) {
                        logger.error(e.getMessage());
                    } else {
                        logger.error("Системная ошибка, невозможно создание асинхронной задачи %s",
                                description);
                    }
                    if (taskData != null && !keys.isEmpty()) {
                        asyncTaskDao.delete(taskData.getId());
                        lockDataService.unlockAllByTask(taskData.getId());
                    } else {
                        lockDataService.unlockMultipleTasks(keys);
                    }
                }
                return false;
            }
        });
    }

    @Override
    public Pair<Boolean, String> restartTask(String lockKey, TAUserInfo user, boolean force, Logger logger) {
        LOG.info(String.format("AsyncManagerImpl.restartTask by %s. lockKey: %s; force: %s", user, lockKey, force));
        LockData lockData = lockDataService.findLock(lockKey);
        if (lockData != null) {
            AsyncTaskData taskData = asyncTaskDao.findByIdLight(lockData.getTaskId());
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


    @Override
    public void interruptTask(final long taskId, final TAUserInfo userInfo, final TaskInterruptCause cause) {
        final AsyncTaskData task = asyncTaskDao.findById(taskId);
        if (task == null) return;

        TAUser currentUser = userInfo.getUser();
        if (userHasPrivilegesToInterruptTask(currentUser, task)) {
            interruptTask(task, userInfo, cause);
        } else {
            throw new ServiceException("Удаление асинхронной задачи не может быть выполнено. Причина: \"недостаточно прав (обратитесь к администратору)\"");
        }
    }

    /**
     * Проверка, достаточно ли у пользователя прав для отмены асинхронной задачи.
     */
    private boolean userHasPrivilegesToInterruptTask(TAUser user, AsyncTaskData task) {

        if (userHasAllTaskPermissions(user)) {
            return true;
        }

        // Контролёр НС (НДФЛ) может отменять задачи только по ТБ, к которым у него есть доступ
        if (user.hasRole(TARole.N_ROLE_CONTROL_NS)) {
            List<Integer> userAvailableTerbankIds = departmentService.findAllAvailableTBIds(user);

            Integer taskTerbankId = getTaskTerbankId(task);
            if (taskTerbankId != null && userAvailableTerbankIds.contains(taskTerbankId)) {
                return true;
            }

            Integer taskCreatorTerbankId = getTaskCreatorTerbankId(task);
            if (taskCreatorTerbankId != null && userAvailableTerbankIds.contains(taskCreatorTerbankId)) {
                return true;
            }
        }

        // Оператор (НДФЛ) может отменять только собственные задачи
        // noinspection RedundantIfStatement // Можно упростить, но лучше, чтобы было видно, что в конце есть return false
        if (user.hasRole(TARole.N_ROLE_OPER) && (user.getId() == task.getUserId())) {
            return true;
        }

        return false;
    }

    /**
     * Имеет ли пользователь все права для работы с асинхронными задачами.
     */
    private boolean userHasAllTaskPermissions(TAUser user) {
        return (user.hasRole(TARole.ROLE_ADMIN) || user.hasRole(TARole.N_ROLE_CONTROL_UNP));
    }

    /**
     * Определение тербанка, по данным которого запущена задача.
     */
    private Integer getTaskTerbankId(AsyncTaskData task) {
        Integer taskTerbankId = null;

        Map<String, Object> taskParams = task.getParams();

        if (taskParams != null) {
            Integer taskDepartmentId = null;

            if (taskParams.containsKey("departmentId")) {
                taskDepartmentId = (Integer) taskParams.get("departmentId");
            }

            if (taskParams.containsKey("departmentReportPeriodId")) {
                Integer departmentReportPeriodId = (Integer) taskParams.get("departmentReportPeriodId");
                DepartmentReportPeriod departmentReportPeriod = departmentReportPeriodService.fetchOne(departmentReportPeriodId);
                taskDepartmentId = departmentReportPeriod.getDepartmentId();
            }

            DeclarationData declaration = null;
            if (taskParams.containsKey("declarationDataId")) {
                Long declarationId = (Long) taskParams.get("declarationDataId");
                declaration = declarationDataService.get(declarationId);
            }
            if (taskParams.containsKey("declaration")) {
                declaration = (DeclarationData) taskParams.get("declaration");
            }
            if (declaration != null) {
                taskDepartmentId = declaration.getDepartmentId();
            }

            if (taskDepartmentId != null) {
                taskTerbankId = departmentService.getParentTBId(taskDepartmentId);
            }
        }

        return taskTerbankId;
    }

    /**
     * Определение тербанка пользователя, запустившего задачу.
     */
    private Integer getTaskCreatorTerbankId(AsyncTaskData task) {
        int taskCreatorId = task.getUserId();
        TAUser taskCreator = userService.getUser(taskCreatorId);
        return departmentService.getParentTBId(taskCreator.getDepartmentId());
    }


    @Override
    public void interruptTask(String lockKey, TAUserInfo user, TaskInterruptCause cause) {
        LockData lockData = lockDataService.findLock(lockKey);
        if (lockData != null) {
            final AsyncTaskData taskData = asyncTaskDao.findByIdLight(lockData.getTaskId());
            interruptTask(taskData, user, cause);
        }
    }

    private void interruptTask(final AsyncTaskData taskData, final TAUserInfo user, final TaskInterruptCause cause) {
        LOG.info(String.format("AsyncManagerImpl.interruptTask by %s. taskData: %s; cause: %s", user, taskData, cause));
        if (taskData != null) {
            tx.executeInNewTransaction(
                    new TransactionLogic() {
                        @Override
                        public Object execute() {
                            try {
                                if (taskData.getState() != AsyncTaskState.CANCELLED) {
                                    List<Integer> waitingUsers = getUsersWaitingForTask(taskData.getId());
                                    if (!waitingUsers.contains(user.getUser().getId())) {
                                        waitingUsers.add(user.getUser().getId());
                                    }
                                    String msg = String.format(AsyncTask.CANCEL_TASK, user.getUser().getName(), taskData.getDescription(), cause.toString());
                                    List<Notification> notifications = new ArrayList<>();
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
                                    asyncTaskDao.updateState(taskData.getId(), AsyncTaskState.CANCELLED);
                                } else {
                                    asyncTaskDao.delete(taskData.getId());
                                }
                            } catch (Exception e) {
                                throw new ServiceException("Удаление асинхронной задачи не может быть выполнено. Причина: \"" + e.getMessage() + "\"", e);
                            }
                            return null;
                        }
                    }
            );
        }
    }


    @Override
    public void finishTask(final long taskId) {
        LOG.info(String.format("AsyncManagerImpl.finishTask. taskId: %s", taskId));
        tx.executeInNewTransaction(new TransactionLogic() {
            @Override
            public Object execute() {
                try {
                    synchronized (AsyncManagerImpl.class) {
                        asyncTaskDao.delete(taskId);
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
        LOG.info(String.format("AsyncManagerImpl.updateState. taskId: %s; state: %s", taskId, state));
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
    public AsyncTaskData reserveTask(final String node, final String priorityNode, final int timeout,
                                     final AsyncQueue balancingVariants, final int maxTasksPerNode) {
        return tx.executeInNewTransaction(new TransactionLogic<AsyncTaskData>() {
            @Override
            public AsyncTaskData execute() {
                AsyncTaskData result = null;
                Long id = asyncTaskDao.reserveTask(node, priorityNode, timeout, balancingVariants, maxTasksPerNode);
                if (id != null) {
                    result = asyncTaskDao.findById(id);
                }
                if (result != null) {
                    LOG.info(String.format("Node '%s' reserved task: %s", node, result));
                }
                return result;
            }
        });
    }

    @Override
    public void addUserWaitingForTask(long taskId, int userId) {
        LOG.info(String.format("AsyncManagerImpl.addUserWaitingForTask. taskId: %s; userId: %s",
                taskId, userId));
        if (asyncTaskDao.isTaskExists(taskId)) {
            if (!asyncTaskDao.findUserIdsWaitingForTask(taskId).contains(userId)) {
                asyncTaskDao.addUserWaitingForTask(taskId, userId);
            }
        } else {
            LOG.warn(String.format("Cannot add subscriber with id = %s to task with id = %s. Cause: task doesn't exists", userId, taskId));
        }
    }

    @Override
    public List<Integer> getUsersWaitingForTask(long taskId) {
        return asyncTaskDao.findUserIdsWaitingForTask(taskId);
    }

    @Override
    public PagingResult<AsyncTaskDTO> getTasks(String filter, PagingParams pagingParams, TAUserInfo userInfo) {
        PagingResult<AsyncTaskDTO> tasks = asyncTaskDao.findAll(filter, pagingParams);

        if (tasks != null && tasks.size() > 0) {
            TAUser user = userInfo.getUser();
            for (AsyncTaskDTO task : tasks) {
                setTaskAllowedToInterruptByUser(task, user);
            }
        }

        return tasks;
    }

    /**
     * Проставить поле AsyncTaskDTO.allowedToInterrupt в зависимости от пользователя
     */
    private void setTaskAllowedToInterruptByUser(AsyncTaskDTO task, TAUser user) {
        if (userHasAllTaskPermissions(user)) {
            task.setAllowedToInterrupt(true);
        } else {
            AsyncTaskData taskData = asyncTaskDao.findById(task.getId());
            task.setAllowedToInterrupt(userHasPrivilegesToInterruptTask(user, taskData));
        }
    }


    @Override
    public void releaseNodeTasks() {
        String currentNode = serverInfo.getServerName();
        LOG.info("Освобождение задач для узла: " + currentNode);
        if (applicationInfo.isProductionMode()) {
            asyncTaskDao.releaseNodeTasks(currentNode);
        } else {
            List<Long> taskIds = asyncTaskDao.findAllByPriorityNode(currentNode);
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
     * Проверить и создать блокировку. Метод делегирует объекту {@param task} проверку на блокировки и в случае успеха создает блокировку для задачи.
     * При конкуррентном вызове метода есть вероятность
     * что 2 потока одновременно проверят возможность установления взаимоисключающих блокировок, а потом одновременно установят
     * взаимоисключающие блокировки. Если сделать реализацию synchronized, то для одного узла исключится такая ситуация.
     *
     * @param operationType задача для которой создается болкировка
     * @param params        параметры задачи
     * @param logger        логгер
     * @param userInfo      информация опользователе
     * @return список установленных блокировок
     */
    private synchronized List<LockData> checkAndCreateLocks(OperationType operationType, Map<String, Object> params, Logger logger, TAUserInfo userInfo) {
        List<Long> declarationDataIdList = null;
        if (params.get("declarationDataId") != null) {
            declarationDataIdList = Collections.singletonList((Long) params.get("declarationDataId"));
        } else if (params.get("declarationDataIds") != null) {
            declarationDataIdList = (List<Long>) params.get("declarationDataIds");
        }
        return declarationLocker.establishLock(declarationDataIdList, operationType, userInfo, logger);
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
}