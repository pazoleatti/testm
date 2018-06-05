package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.AsyncTask;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.dao.AsyncTaskDao;
import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ScriptServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Абстрактная реализация асинхронной задачи.
 * В ней реализована общая часть логики взаимодействия с блокировками объектов, для которых выполняется бизнес-логика конкретных задач
 *
 * @author dloshkarev
 */
public abstract class AbstractAsyncTask implements AsyncTask {

    private static final Log LOG = LogFactory.getLog(AbstractAsyncTask.class);

    @Autowired
    private AsyncManager asyncManager;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private TransactionHelper tx;
    @Autowired
    private LogEntryService logEntryService;
    @Autowired
    private AsyncTaskDao asyncTaskDao;
    @Autowired
    protected TAUserService userService;
    @Autowired
    private ConfigurationDao configurationDao;


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

    private static final ThreadLocal<String> TIME_ZONE_MOSCOW = new ThreadLocal<String>() {
        @Override
        protected String initialValue() {
            return "Europe/Moscow";
        }
    };

    /**
     * Выполнение бизнес логики задачи
     *
     * @param taskData данные задачи
     */
    protected abstract BusinessLogicResult executeBusinessLogic(AsyncTaskData taskData, Logger logger) throws InterruptedException;

    /**
     * Возвращает текст оповещения, которое будет создано для пользователей, ожидающих выполнение этой задачи
     *
     * @return текст сообщения
     */
    protected abstract String getNotificationMsg(AsyncTaskData taskData);

    /**
     * Возвращает текст оповещения, которое будет создано для пользователей в случае некорректного завершения задачи
     *
     * @return текст сообщения
     */
    protected abstract String getErrorMsg(AsyncTaskData taskData, boolean unexpected);

    /**
     * Проверяет критерии выполнения задачи. Если ее возможно выполнить, то возвращает очередь, иначе выбрасывает исключение с описанием того, какие критерии были нарушены
     *
     * @return очередь, в которую будет отправлена задача, если ее возможно выполнить
     */
    protected abstract AsyncQueue checkTaskLimit(String taskDescription, TAUserInfo user, Map<String, Object> params, Logger logger) throws AsyncTaskException;

    /**
     * Возвращает тип асинхронной задачи в конкретной реализации
     */
    protected abstract AsyncTaskType getAsyncTaskType();

    /**
     * Выполняет обработку результата проверки лимитов задачи
     *
     * @param value    вычисленное значение лимита
     * @param taskName название задачи
     * @param msg      описание результата проверки лимитов
     * @return очередь в которую будет направлена задача
     * @throws AsyncTaskException в случае, если проверка не была пройдена
     */
    protected AsyncQueue checkTask(Long value, String taskName, String msg) throws AsyncTaskException {
        AsyncTaskTypeData taskTypeData = asyncTaskDao.getTaskTypeData(getAsyncTaskType().getAsyncTaskTypeId());
        if (taskTypeData == null) {
            throw new AsyncTaskException(String.format("Cannot find task parameters for \"%s\"", taskName));
        }
        if (taskTypeData.getTaskLimit() != 0 && taskTypeData.getTaskLimit() < value) {
            Logger logger = new Logger();
            logger.error("Критерии возможности выполнения задач задаются в конфигурационных параметрах (параметры асинхронных заданий). За разъяснениями обратитесь к Администратору");
            throw new AsyncTaskException(new ServiceLoggerException(AsyncTask.CHECK_TASK,
                    logEntryService.save(logger.getEntries()),
                    taskName,
                    String.format(msg, taskTypeData.getTaskLimit())));
        } else if (taskTypeData.getShortQueueLimit() == 0 || taskTypeData.getShortQueueLimit() >= value) {
            return AsyncQueue.SHORT;
        }
        return AsyncQueue.LONG;
    }

    @Override
    public void execute(final AsyncTaskData taskData) {
        final Logger logger = new Logger();
        final Date startDate = Calendar.getInstance(TimeZone.getTimeZone(TIME_ZONE_MOSCOW.get())).getTime();

        Configuration shotTimingConfiguration = configurationDao.fetchByEnum(ConfigurationParam.SHOW_TIMING);
        final boolean isShowTiming = "1".equals(shotTimingConfiguration.getValue());
        if (isShowTiming) {
            logger.info("Начало выполнения операции %s", sdf_time.get().format(startDate));
        }
        asyncManager.updateState(taskData.getId(), AsyncTaskState.STARTED);
        final BusinessLogicResult taskStatus = tx.executeInNewTransaction(new TransactionLogic<BusinessLogicResult>() {
            @Override
            public BusinessLogicResult execute() {
                try {
                    LOG.info(String.format("Business logic execution has been started for task with id %s", taskData.getId()));
                    BusinessLogicResult taskStatus = executeBusinessLogic(taskData, logger);
                    LOG.debug("Business logic execution is complete");
                    Date endDate = Calendar.getInstance(TimeZone.getTimeZone(TIME_ZONE_MOSCOW.get())).getTime();
                    if (isShowTiming) {
                        logger.info("Длительность выполнения операции: %d мс (%s - %s)", (endDate.getTime() - startDate.getTime()), sdf_time.get().format(startDate), sdf_time.get().format(endDate));
                    }
                    return taskStatus;
                } catch (final Throwable e) {
                    LOG.error(String.format("Exception occurred during execution of async task with id %s", taskData.getId()), e);
                    tx.executeInNewTransaction(new TransactionLogic() {
                        @Override
                        public Object execute() {
                            LOG.info(String.format("Sending error notification for async task with id %s", taskData.getId()));
                            if (AsyncTaskType.CHECK_DEC.equals(taskData.getType())) {
                                asyncManager.updateState(taskData.getId(), AsyncTaskState.SENDING_MSGS);
                            } else {
                                asyncManager.updateState(taskData.getId(), AsyncTaskState.SENDING_ERROR_MSGS);
                            }
                            Date endDate = new Date();
                            String msg = (e instanceof ScriptServiceException) ? getErrorMsg(taskData, false) : getErrorMsg(taskData, true);
                            logger.getEntries().add(0, new LogEntry(LogLevel.ERROR, msg));
                            if (e.getMessage() != null && !e.getMessage().isEmpty()) {
                                logger.error(e);
                            }
                            if (isShowTiming) {
                                logger.info("Длительность выполнения операции: %d мс (%s - %s)", (endDate.getTime() - startDate.getTime()), sdf_time.get().format(startDate), sdf_time.get().format(endDate));
                            }
                            sendNotifications(taskData, msg, logEntryService.save(logger.getEntries()), NotificationType.DEFAULT, null);
                            return null;
                        }
                    });
                    LOG.info(String.format("Rollback transaction for async task with id %s", taskData.getId()));
                    if (e instanceof ServiceLoggerException) {
                        throw new ServiceLoggerException("Cannot execute async task", ((ServiceLoggerException) e).getUuid());
                    } else {
                        throw new RuntimeException("Cannot execute async task", e);
                    }
                }
            }
        });

        tx.executeInNewTransaction(new TransactionLogic() {
            @Override
            public Object execute() {
                try {
                    LOG.info(String.format("Storing notifications for task with id %s", taskData.getId()));
                    asyncManager.updateState(taskData.getId(), AsyncTaskState.SAVING_MSGS);
                    String msg = taskStatus.isSuccess() ? getNotificationMsg(taskData) : getErrorMsg(taskData, taskStatus.isUnexpected());
                    logger.getEntries().add(0, new LogEntry(taskStatus.isSuccess() ? LogLevel.INFO : LogLevel.ERROR, msg));
                    String uuid = logEntryService.save(logger.getEntries());
                    asyncManager.updateState(taskData.getId(), AsyncTaskState.SENDING_MSGS);
                    sendNotifications(taskData, msg, uuid, taskStatus.getNotificationType(), taskStatus.getReportId());
                    asyncManager.finishTask(taskData.getId());
                } catch (Exception e) {
                    LOG.error("Error occurred during sending notifications", e);
                }
                return null;
            }
        });

        LOG.info(String.format("Async task with id %s complete successfully", taskData.getId()));
    }

    @Override
    public AsyncQueue checkTaskLimit(String taskDescription, TAUserInfo userInfo, Map<String, Object> params) throws AsyncTaskException {
        Logger logger = new Logger();
        AsyncQueue result = checkTaskLimit(taskDescription, userInfo, params, logger);
        if (logger.containsLevel(LogLevel.ERROR)) {
            throw new ServiceLoggerException(
                    "Произошла ошибка при проверке ограничений асинхронной задачи \"" + taskDescription + "\"",
                    logEntryService.save(logger.getEntries()));
        }
        return result;
    }

    /**
     * Отправка уведомлений подписчикам на указанную задачу
     */
    protected void sendNotifications(AsyncTaskData taskData, String msg, String uuid, NotificationType notificationType, String reportId) {
        LOG.info(String.format("Sending notification for async task with id %s", taskData.getId()));
        if (msg != null && !msg.isEmpty()) {
            //Получаем список пользователей-подписчиков, для которых надо сформировать оповещение
            List<Integer> waitingUsers = asyncManager.getUsersWaitingForTask(taskData.getId());
            if (!waitingUsers.isEmpty()) {
                List<Notification> notifications = new ArrayList<Notification>();
                for (Integer userId : waitingUsers) {
                    Notification notification = new Notification();
                    notification.setUserId(userId);
                    notification.setCreateDate(new Date());
                    notification.setText(msg);
                    notification.setLogId(uuid);
                    notification.setReportId(reportId);
                    notification.setNotificationType(notificationType);
                    notifications.add(notification);
                }
                //Создаем оповещение для каждого пользователя из списка
                notificationService.create(notifications);
            }
        }
        LOG.info(String.format("Sending notification for async task with id %s completed", taskData.getId()));
    }

    /**
     * Класс с результатом выполнения бизнес-логики
     */
    protected class BusinessLogicResult {
        private final String reportId;
        private final boolean success;
        private final boolean unexpected;
        private final NotificationType notificationType;

        public BusinessLogicResult(boolean success, boolean unexpected, NotificationType notificationType, String reportId) {
            this.reportId = reportId;
            this.notificationType = notificationType;
            this.success = success;
            this.unexpected = unexpected;
        }

        public BusinessLogicResult(boolean success, NotificationType notificationType, String reportId) {
            this(success, false, notificationType, reportId);
        }

        public BusinessLogicResult(boolean success, String reportId) {
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

}
