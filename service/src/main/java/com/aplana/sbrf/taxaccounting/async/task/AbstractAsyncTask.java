package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.async.AsyncTask;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.dao.AsyncTaskTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ScriptServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.NotificationService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.service.TransactionHelper;
import com.aplana.sbrf.taxaccounting.service.TransactionLogic;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

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
    private AsyncTaskTypeDao asyncTaskTypeDao;
    @Autowired
    protected TAUserService userService;
    @Autowired
    private ConfigurationDao configurationDao;

    static final FastDateFormat SDF_DD_MM_YYYY = FastDateFormat.getInstance("dd.MM.yyyy", TimeZone.getTimeZone("GMT+3"));
    private static final FastDateFormat sdf_time = FastDateFormat.getInstance("HH:mm:ss.SSS", TimeZone.getTimeZone("GMT+3"));

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
        AsyncTaskTypeData taskTypeData = asyncTaskTypeDao.findById(getAsyncTaskType().getId());
        if (taskTypeData == null) {
            throw new AsyncTaskException(String.format("Cannot find task parameters for \"%s\"", taskName));
        }
        if (taskTypeData.getTaskLimit() != null && taskTypeData.getTaskLimit() != 0 && taskTypeData.getTaskLimit() < value) {
            throw new ServiceException(AsyncTask.CHECK_TASK, taskName);
        } else if (taskTypeData.getShortQueueLimit() == null || taskTypeData.getShortQueueLimit() == 0 || taskTypeData.getShortQueueLimit() >= value) {
            return AsyncQueue.SHORT;
        }
        return AsyncQueue.LONG;
    }

    @Override
    public void execute(final AsyncTaskData taskData) {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+3"));
        final Logger logger = logEntryService.createLogger();
        final Date startDate = Calendar.getInstance().getTime();

        Configuration shotTimingConfiguration = configurationDao.fetchByEnum(ConfigurationParam.SHOW_TIMING);
        final boolean isShowTiming = "1".equals(shotTimingConfiguration.getValue());
        if (isShowTiming) {
            logger.info("Начало выполнения операции %s", sdf_time.format(startDate));
        }
        asyncManager.updateState(taskData.getId(), AsyncTaskState.STARTED);
        final BusinessLogicResult taskStatus = tx.executeInNewTransaction(new TransactionLogic<BusinessLogicResult>() {
            @Override
            public BusinessLogicResult execute() {
                try {
                    LOG.info(String.format("Business logic execution has been started for task with id %s", taskData.getId()));
                    BusinessLogicResult taskStatus = executeBusinessLogic(taskData, logger);
                    LOG.debug("Business logic execution is complete");
                    Date endDate = Calendar.getInstance(TimeZone.getTimeZone("GMT+3")).getTime();
                    if (isShowTiming) {
                        logger.info("Длительность выполнения операции: %d мс (%s - %s)", (endDate.getTime() - startDate.getTime()), sdf_time.format(startDate), sdf_time.format(endDate));
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

                            // Кладём в данные о таске ошибку для возможности формирования текста на её основе
                            taskData.getParams().put("exceptionThrown", e);

                            // Извлекаем из логгера тексты ошибок и складываем их в данные о таске
                            String errorsText = collectErrorsText(logger);
                            taskData.getParams().put("errorsText", errorsText);

                            // Формирование текста оповещения (для верхнего меню)
                            boolean isExpectedScriptException = (e instanceof ScriptServiceException) || (e instanceof ServiceLoggerException);
                            String notification = getErrorMsg(taskData, !isExpectedScriptException);

                            // Заполнение уведомлений (нижняя панель с сообщениями)
                            // На первом месте текст из оповещения
                            logger.getEntries().add(0, new LogEntry(LogLevel.ERROR, notification));
                            // В конце текст из ошибки
                            if (e.getMessage() != null && !e.getMessage().isEmpty()) {
                                logger.error(e);
                            }
                            // Если нужно, добавляем время выполнения скрипта
                            if (isShowTiming) {
                                Date endDate = new Date();
                                logger.info("Длительность выполнения операции: %d мс (%s - %s)",
                                        (endDate.getTime() - startDate.getTime()),
                                        sdf_time.format(startDate),
                                        sdf_time.format(endDate));
                            }

                            // Публикация оповещений всем подписчикам
                            logEntryService.save(logger);
                            sendNotifications(taskData, notification, logger.getLogId(), NotificationType.DEFAULT, null);
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
                    logger.getEntries().add(0, new LogEntry(taskStatus.getLogLevel(), msg));
                    logEntryService.save(logger);
                    String uuid = logger.getLogId();
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

    /**
     * Сбор текстов ошибок из логгера
     *
     * @param logger внутренний логгер
     * @return сборный текст ошибок из логгера
     */
    private String collectErrorsText(Logger logger) {
        StringBuilder errorsText = new StringBuilder();
        for (LogEntry entry : logger.getEntries()) {
            if (entry.getLevel().equals(LogLevel.ERROR)) {
                errorsText.append(entry.getMessage());
            }
        }
        return errorsText.toString();
    }


    @Override
    public AsyncQueue defineTaskLimit(String taskDescription, TAUserInfo userInfo, Map<String, Object> params) throws AsyncTaskException {
        Logger logger = new Logger();
        return checkTaskLimit(taskDescription, userInfo, params, logger);
    }

    /**
     * Отправка оповещений подписчикам на указанную задачу
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
        private final boolean success;
        private final LogLevel logLevel;
        private final boolean unexpected;
        private final NotificationType notificationType;
        private final String reportId;

        public BusinessLogicResult(boolean success, LogLevel logLevel, boolean unexpected, NotificationType notificationType, String reportId) {
            this.success = success;
            this.logLevel = logLevel;
            this.unexpected = unexpected;
            this.notificationType = notificationType;
            this.reportId = reportId;
        }

        public BusinessLogicResult(boolean success, boolean unexpected, NotificationType notificationType, String reportId) {
            this(success, success ? LogLevel.INFO : LogLevel.ERROR, unexpected, notificationType, reportId);
        }

        public BusinessLogicResult(boolean success, NotificationType notificationType, String reportId) {
            this(success, success ? LogLevel.INFO : LogLevel.ERROR, false, notificationType, reportId);
        }

        public BusinessLogicResult(boolean success, String reportId) {
            this(success, success ? LogLevel.INFO : LogLevel.ERROR, false, NotificationType.DEFAULT, reportId);
        }

        public BusinessLogicResult(LogLevel logLevel, boolean success) {
            this(success, logLevel, false, NotificationType.DEFAULT, null);
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

        public LogLevel getLogLevel() {
            return logLevel;
        }

        public boolean isUnexpected() {
            return unexpected;
        }
    }

}
