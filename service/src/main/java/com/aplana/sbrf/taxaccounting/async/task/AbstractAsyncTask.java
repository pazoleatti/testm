package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.AsyncTask;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.AsyncTaskDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ScriptServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.exception.TAInterruptedException;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.NotificationService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.util.TransactionHelper;
import com.aplana.sbrf.taxaccounting.util.TransactionLogic;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.aplana.sbrf.taxaccounting.async.AsyncTask.RequiredParams.*;

/**
 * Абстрактная реализация асинхронной задачи.
 * В ней реализована общая часть логики взаимодействия с блокировками объектов, для которых выполняется бизнес-логика конкретных задач
 * @author dloshkarev
 */
public abstract class AbstractAsyncTask implements AsyncTask {

    private static final Log LOG = LogFactory.getLog(AbstractAsyncTask.class);

    protected static final String COMPLETE_FORM =
            "Сформирован %s отчет:";
    protected static final String ERROR_FORM =
            "Произошла непредвиденная ошибка при формировании %s отчета:";
    private static final long MAX_WAIT_TIMEOUT = 2000;

    @Autowired
    private LockDataService lockService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private TransactionHelper transactionHelper;
    @Autowired
    private LogEntryService logEntryService;
    @Autowired
    private AsyncTaskDao asyncTaskTypeDao;
    @Autowired
    protected TAUserService userService;

    protected class TaskStatus {
        private final String reportId;
        private final boolean success;
        private final boolean unexpected;
        private final NotificationType notificationType;

        public TaskStatus(boolean success, boolean unexpected, NotificationType notificationType, String reportId) {
            this.reportId = reportId;
            this.notificationType = notificationType;
            this.success = success;
            this.unexpected = unexpected;
        }

        public TaskStatus(boolean success, NotificationType notificationType, String reportId) {
            this(success, false, notificationType, reportId);
        }

        public TaskStatus(boolean success, String reportId) {
            this(success, false, NotificationType.DEFAULT, reportId);
        }

        public NotificationType getNotificationType() {
            return notificationType;
        }

        public String getReportId() {
            return reportId;
        }

        public boolean isSuccess() {
            return success;
        }

        public boolean isUnexpected() {
            return unexpected;
        }
    }

    private static final ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        }
    };
    protected static final ThreadLocal<SimpleDateFormat> SDF_DD_MM_YYYY = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };
    private static final ThreadLocal<SimpleDateFormat> sdf_time = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("HH:mm:ss.SSS");
        }
    };

    /**
     * Выполнение бизнес логики задачи
     * @param params параметры
     */
    protected abstract TaskStatus executeBusinessLogic(Map<String, Object> params, Logger logger) throws InterruptedException;

    /**
     * Возвращает название задачи. Используется при выводе ошибок.
     * @return название задачи
     */
    protected abstract String getAsyncTaskName();

    /**
     * Возвращает описание основной операции, входящей в выполнение бизнес-логики
     * Используется при смене статуса задачи
     * @return описание
     */
    protected String getBusinessLogicTitle() {
        return getAsyncTaskName();
    }

    /**
     * Возвращает текст оповещения, которое будет создано для пользователей, ожидающих выполнение этой задачи
     * @return текст сообщения
     */
    protected abstract String getNotificationMsg(Map<String, Object> params);

    /**
     * Возвращает текст оповещения, которое будет создано для пользователей в случае некорректного завершения задачи
     * @return текст сообщения
     */
    protected abstract String getErrorMsg(Map<String, Object> params, boolean unexpected);

    private interface CheckLockHandler {
        void checkLock();
    }

    private final class ProcessRunner implements Runnable {
        private Thread thread;
        private CheckLockHandler checkLockHandler;
        private boolean canceled = false;

        private ProcessRunner(Thread thread, CheckLockHandler checkLockHandler) {
            this.thread = thread;
            this.checkLockHandler = checkLockHandler;
        }

        public void cancel() {
            this.canceled = true;
        }

        @Override
        public void run() {
            try {
                while (!canceled) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        canceled = true;
                    }
                    checkLockHandler.checkLock();
                }
            } catch (Exception e) {
                if (thread.isAlive() && !canceled) {
                    thread.interrupt();
                }
            }
        }
    }

    protected BalancingVariants checkTask(ReportType reportType, Long value, String taskName, String msg) throws AsyncTaskException {
        AsyncTaskTypeData taskTypeData = asyncTaskTypeDao.getTaskData(reportType.getAsyncTaskTypeId());
        if (taskTypeData == null) {
            throw new AsyncTaskException(String.format("Cannot find task parameters for \"%s\"", taskName));
        }
        if (taskTypeData.getTaskLimit() != 0 && taskTypeData.getTaskLimit() < value) {
            Logger logger = new Logger();
            logger.error("Критерии возможности выполнения задач задаются в конфигурационных параметрах (параметры асинхронных заданий). За разъяснениями обратитесь к Администратору");
            throw new AsyncTaskException(new ServiceLoggerException(LockData.CHECK_TASK,
                    logEntryService.save(logger.getEntries()),
                    taskName,
                    String.format(msg, taskTypeData.getTaskLimit())));
        } else if (taskTypeData.getShortQueueLimit() == 0 || taskTypeData.getShortQueueLimit() >= value) {
            return BalancingVariants.SHORT;
        }
        return BalancingVariants.LONG;
    }

    @Override
    public void execute(final Map<String, Object> params) {
        final String lock = (String) params.get(LOCKED_OBJECT.name());
        final Date lockDate = (Date) params.get(LOCK_DATE.name());
        final Logger logger = new Logger();
        final Date startDate = new Date();
        logger.info("Начало выполнения операции %s", sdf_time.get().format(startDate));
        LOG.info(String.format("Async task with key %s and start date %s has been started: (%s)", lock, sdf.get().format(lockDate), lockDate.getTime()));
        lockService.updateState(lock, lockDate, LockData.State.STARTED.getText());
        final TaskStatus taskStatus;
        try {
            taskStatus = transactionHelper.executeInNewTransaction(new TransactionLogic<TaskStatus>() {
                @Override
                public TaskStatus execute() {
                    try {
                        if (lockService.isLockExists(lock, lockDate)) {
                            LOG.info(String.format("Business logic execution has been started for task with key %s", lock));
                            lockService.updateState(lock, lockDate, getBusinessLogicTitle());
                            //Если блокировка на объект задачи все еще существует, значит на нем можно выполнять бизнес-логику
                            ProcessRunner runner = new ProcessRunner(Thread.currentThread(), new CheckLockHandler() {
                                @Override
                                public void checkLock() {
                                    if (!lockService.isLockExists(lock, lockDate)) {
                                        throw new RuntimeException(String.format("Async task with key %s and start date %s (%s) has been expired", lock, sdf.get().format(lockDate), lockDate.getTime()));
                                    }
                                }
                            });
                            Thread threadRunner = new Thread(Thread.currentThread().getThreadGroup(), runner);
                            TaskStatus taskStatus;
                            try {
                                threadRunner.start();
                                taskStatus = executeBusinessLogic(params, logger);
                                LOG.debug("Business logic execution is complete");
                            } finally {
                                if (threadRunner.isAlive()) {
                                    runner.cancel();
                                    try {
                                        threadRunner.join(MAX_WAIT_TIMEOUT);
                                    } catch (InterruptedException e){
                                        // nothing
                                    }
                                }
                                if (Thread.interrupted()) {
                                    throw new TAInterruptedException();
                                }
                            }
                            Date endDate = new Date();
                            logger.info("Длительность выполнения операции: %d мс (%s - %s)", (endDate.getTime() - startDate.getTime()), sdf_time.get().format(startDate), sdf_time.get().format(endDate));
                            if (!lockService.isLockExists(lock, lockDate)) {
                                //Если после выполнения бизнес логики, оказывается, что блокировки уже нет
                                //Значит результаты нам уже не нужны - откатываем транзакцию и все изменения
                                throw new RuntimeException(String.format("Async task %s result has been expired. Next task from queue will be processing", lock));
                            }
                            return taskStatus;
                        } else {
                            throw new RuntimeException(String.format("Async task %s has been expired.", lock));
                        }
                    } catch (final Throwable e) {
                        LOG.error(String.format("Exception occurred during execution of async task with key %s and start date %s (%s)",
                                lock, sdf.get().format(lockDate), lockDate.getTime()), e);
                        if (lockService.isLockExists(lock, lockDate)) {
                            try {
                                transactionHelper.executeInNewTransaction(new TransactionLogic() {
                                    @Override
                                    public Object execute() {
                                        LOG.info(String.format("Sending error notification for async task with key %s", lock));
                                        if (ReportType.CHECK_DEC.equals(getReportType())
                                                || ReportType.CHECK_FD.equals(getReportType())) {
                                            lockService.updateState(lock, lockDate, LockData.State.SENDING_MSGS.getText());
                                        } else {
                                            lockService.updateState(lock, lockDate, LockData.State.SENDING_ERROR_MSGS.getText());
                                        }
                                        Date endDate = new Date();
                                        String msg;
                                        if (e instanceof ScriptServiceException) {
                                            msg = getErrorMsg(params, false);
                                        } else {
                                            msg = getErrorMsg(params, true);
                                        }
                                        logger.getEntries().add(0, new LogEntry(LogLevel.ERROR, msg));
                                        if (e.getMessage() != null && !e.getMessage().isEmpty()) logger.error(e);
                                        logger.info("Длительность выполнения операции: %d мс (%s - %s)", (endDate.getTime() - startDate.getTime()), sdf_time.get().format(startDate), sdf_time.get().format(endDate));
                                        sendNotifications(lock, msg, logEntryService.save(logger.getEntries()), NotificationType.DEFAULT, null);
                                        return null;
                                    }
                                });
                            } finally {
                                LOG.info(String.format("Unlock async task with key %s", lock));
                                lockService.unlock(lock, (Integer) params.get(USER_ID.name()));
                            }
                        }
                        LOG.info(String.format("Rollback transaction for async task with key %s", lock));
                        if (e instanceof ServiceLoggerException) {
                            throw new ServiceLoggerException("Cannot execute async task", ((ServiceLoggerException) e).getUuid());
                        } else {
                            throw new RuntimeException("Cannot execute async task", e);
                        }
                    }
                }
            });
        } catch (final TransactionException e) {
            transactionHelper.executeInNewTransaction(new TransactionLogic() {
                @Override
                public Object execute() {
                    if (lockService.isLockExists(lock, lockDate)) {
                        try {
                            String msg = getErrorMsg(params, true);
                            logger.getEntries().add(0, new LogEntry(LogLevel.ERROR, msg));
                            if (e.getMessage() != null && !e.getMessage().isEmpty()) {
                                logger.error(e);
                            }
                            sendNotifications(lock, msg, logEntryService.save(logger.getEntries()), NotificationType.DEFAULT, null);
                        } finally {
                            lockService.unlock(lock, (Integer) params.get(USER_ID.name()));
                        }
                    }
                    return null;
                }
            });
            LOG.info(String.format("Rollback transaction for async task with key %s", lock));
            throw new RuntimeException("Cannot execute async task", e);
        }

        transactionHelper.executeInNewTransaction(new TransactionLogic() {
            @Override
            public Object execute() {
                try {
                    LOG.info(String.format("Storing notifications for task with key %s", lock));
                    lockService.updateState(lock, lockDate, LockData.State.SAVING_MSGS.getText());
                    String msg = taskStatus.isSuccess() ? getNotificationMsg(params) : getErrorMsg(params, taskStatus.isUnexpected());
                    logger.getEntries().add(0, new LogEntry(taskStatus.isSuccess() ? LogLevel.INFO : LogLevel.ERROR, msg));
                    String uuid = logEntryService.save(logger.getEntries());
                    lockService.updateState(lock, lockDate, LockData.State.SENDING_MSGS.getText());
                    sendNotifications(lock, msg, uuid, taskStatus.getNotificationType(), taskStatus.getReportId());
                } catch (Exception e) {
                    LOG.error("Error occurred during sending notifications", e);
                }
                return null;
            }
        });

        try {
            LOG.info(String.format("Post processing for task with key %s", lock));
            lockService.updateState(lock, lockDate, LockData.State.POST_LOGIC.getText());
            executePostLogic(params);
        } finally {
            LOG.info(String.format("Unlock task with key %s after successful execution", lock));
            lockService.unlock(lock, (Integer) params.get(USER_ID.name()));
        }
        LOG.info(String.format("Async task with key %s complete successfully", lock));
    }

    @Override
    public BalancingVariants checkTaskLimit(Map<String, Object> params) throws AsyncTaskException {
        Logger logger = new Logger();
        BalancingVariants result = checkTaskLimit(params, logger);
        if (logger.containsLevel(LogLevel.ERROR)) {
            throw new ServiceLoggerException(
                    "Произошла ошибка при проверке ограничений асинхронной задачи \"" + getAsyncTaskName() + "\"",
                    logEntryService.save(logger.getEntries()));
        }
        return result;
    }

    protected abstract BalancingVariants checkTaskLimit(Map<String, Object> params, Logger logger) throws AsyncTaskException;

    /**
     * Выполнение работ после завершения основной задачи и отправки уведомлений
     * Действия выполняются в отдельной транзакции
     * @param params
     */
    protected void executePostLogic(Map<String, Object> params) {
        //
    }

    protected boolean isProductionMode() {
        throw new UnsupportedOperationException();
    }

    protected abstract ReportType getReportType();

    /**
     * Отправка уведомлений подисчикам на указанную блокировку
     * @param lock ключ блокировки
     */
    private void sendNotifications(String lock, String msg, String uuid, NotificationType notificationType, String reportId) {
        LOG.info(String.format("Sending notification for async task with key %s", lock));
        if (msg != null && !msg.isEmpty()) {
            //Получаем список пользователей-подписчиков, для которых надо сформировать оповещение
            List<Integer> waitingUsers = lockService.getUsersWaitingForLock(lock);
            if (!waitingUsers.isEmpty()) {
                List<Notification> notifications = new ArrayList<Notification>();
                for (Integer userId : waitingUsers) {
                    Notification notification = new Notification();
                    notification.setUserId(userId);
                    notification.setCreateDate(new LocalDateTime());
                    notification.setText(msg);
                    notification.setLogId(uuid);
                    notification.setReportId(reportId);
                    notification.setNotificationType(notificationType);
                    notifications.add(notification);
                }
                //Создаем оповещение для каждого пользователя из списка
                notificationService.saveList(notifications);
            }
        }
        LOG.info(String.format("Sending notification for async task with key %s completed", lock));
    }

    protected TAUserInfo getUserInfo(Map<String, Object> params) {
        int userId = (Integer)params.get(USER_ID.name());
        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(userId));
        return userInfo;
    }

}
