package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.dao.AsyncTaskTypeDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ScriptServiceException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.util.StringUtils;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.NotificationService;
import com.aplana.sbrf.taxaccounting.util.TransactionHelper;
import com.aplana.sbrf.taxaccounting.util.TransactionLogic;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.*;

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

    @Autowired
    private LockDataService lockService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private TransactionHelper transactionHelper;
    @Autowired
    private LogEntryService logEntryService;
    @Autowired
    private AsyncTaskTypeDao asyncTaskTypeDao;
    @Autowired
    private RefBookFactory refBookFactory;

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

    protected BalancingVariants checkTask(ReportType reportType, Long value, String taskName, String msg) throws AsyncTaskException {
        AsyncTaskTypeData taskTypeData = asyncTaskTypeDao.get(reportType.getAsyncTaskTypeId());
        if (taskTypeData == null) {
            throw new AsyncTaskException(String.format("Не найдены параметры задачи \"%s\"", taskName));
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
        LOG.info(String.format("Запущена асинхронная задача с ключом %s и датой начала %s (%s)", lock, sdf.get().format(lockDate), lockDate.getTime()));
        lockService.updateState(lock, lockDate, LockData.State.STARTED.getText());
        final TaskStatus taskStatus;
        try {
            taskStatus = transactionHelper.executeInNewTransaction(new TransactionLogic<TaskStatus>() {
                @Override
                public TaskStatus execute() {
                    try {
                        if (lockService.isLockExists(lock, lockDate)) {
                            LOG.info(String.format("Для задачи с ключом %s запущено выполнение бизнес-логики", lock));
                            lockService.updateState(lock, lockDate, getBusinessLogicTitle());
                            //Если блокировка на объект задачи все еще существует, значит на нем можно выполнять бизнес-логику
                            TaskStatus taskStatus = executeBusinessLogic(params, logger);
                            if (!lockService.isLockExists(lock, lockDate)) {
                                //Если после выполнения бизнес логики, оказывается, что блокировки уже нет
                                //Значит результаты нам уже не нужны - откатываем транзакцию и все изменения
                                throw new RuntimeException(String.format("Результат выполнения задачи %s больше не актуален. Выполняется переход к следующей задаче в очереди", lock));
                            }
                            return taskStatus;
                        } else {
                            throw new RuntimeException(String.format("Задача %s больше не актуальна.", lock));
                        }
                    } catch (final Throwable e) {
                        LOG.error(String.format("Произошла ошибка при выполнении асинхронной задачи с ключом %s и датой начала %s (%s)",
                                lock, sdf.get().format(lockDate), lockDate.getTime()), e);
                        if (lockService.isLockExists(lock, lockDate)) {
                            try {
                                transactionHelper.executeInNewTransaction(new TransactionLogic() {
                                    @Override
                                    public Object execute() {
                                        LOG.info(String.format("Для задачи с ключом %s выполняется рассылка уведомлений об ошибке", lock));
                                        if (ReportType.CHECK_DEC.equals(getReportType())
                                                || ReportType.CHECK_FD.equals(getReportType())) {
                                            lockService.updateState(lock, lockDate, LockData.State.SENDING_MSGS.getText());
                                        } else {
                                            lockService.updateState(lock, lockDate, LockData.State.SENDING_ERROR_MSGS.getText());
                                        }
                                        if (e instanceof ServiceLoggerException && ((ServiceLoggerException) e).getUuid() != null) {
                                            String msg = getErrorMsg(params, true);
                                            Logger logger1 = new Logger();
                                            logger1.error(msg);
                                            if (e.getMessage() != null && !e.getMessage().isEmpty()) logger1.error(e);
                                            sendNotifications(lock, msg, logEntryService.addFirst(logger1.getEntries(), ((ServiceLoggerException) e).getUuid()), NotificationType.DEFAULT, null);
                                        } else if (e instanceof ScriptServiceException) {
                                            String msg = getErrorMsg(params, false);
                                            logger.getEntries().add(0, new LogEntry(LogLevel.ERROR, msg));
                                            if (e.getMessage() != null && !e.getMessage().isEmpty()) logger.error(e);
                                            sendNotifications(lock, msg, logEntryService.save(logger.getEntries()), NotificationType.DEFAULT, null);
                                        } else {
                                            String msg = getErrorMsg(params, true);
                                            logger.getEntries().add(0, new LogEntry(LogLevel.ERROR, msg));
                                            if (e.getMessage() != null && !e.getMessage().isEmpty()) logger.error(e);
                                            sendNotifications(lock, msg, logEntryService.save(logger.getEntries()), NotificationType.DEFAULT, null);
                                        }
                                        return null;
                                    }
                                });
                            } finally {
                                LOG.info(String.format("Для задачи с ключом %s выполняется снятие блокировки", lock));
                                lockService.unlock(lock, (Integer) params.get(USER_ID.name()));
                            }
                        }
                        LOG.info(String.format("Для задачи с ключом %s выполняется откат транзакции", lock));
                        if (e instanceof ServiceLoggerException) {
                            throw new ServiceLoggerException("Не удалось выполнить асинхронную задачу", ((ServiceLoggerException) e).getUuid());
                        } else {
                            throw new RuntimeException("Не удалось выполнить асинхронную задачу", e);
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
                            if (e.getMessage() != null && !e.getMessage().isEmpty())
                            logger.error(e);
                            sendNotifications(lock, msg, logEntryService.save(logger.getEntries()), NotificationType.DEFAULT, null);
                        } finally {
                            lockService.unlock(lock, (Integer) params.get(USER_ID.name()));
                        }
                    }
                    return null;
                }
            });
            LOG.info(String.format("Для задачи с ключом %s выполняется откат транзакции", lock));
            throw new RuntimeException("Не удалось выполнить асинхронную задачу", e);
        }

        transactionHelper.executeInNewTransaction(new TransactionLogic() {
            @Override
            public Object execute() {
                try {
                    LOG.info(String.format("Для задачи с ключом %s выполняется сохранение сообщений", lock));
                    lockService.updateState(lock, lockDate, LockData.State.SAVING_MSGS.getText());
                    String msg = taskStatus.isSuccess() ? getNotificationMsg(params) : getErrorMsg(params, taskStatus.isUnexpected());
                    logger.getEntries().add(0, new LogEntry(taskStatus.isSuccess() ? LogLevel.INFO : LogLevel.ERROR, msg));
                    String uuid = logEntryService.save(logger.getEntries());
                    LOG.info(String.format("Для задачи с ключом %s выполняется рассылка уведомлений", lock));
                    lockService.updateState(lock, lockDate, LockData.State.SENDING_MSGS.getText());
                    sendNotifications(lock, msg, uuid, taskStatus.getNotificationType(), taskStatus.getReportId());
                } catch (Exception e) {
                    LOG.error("Произошла ошибка при рассылке сообщений", e);
                }
                return null;
            }
        });

        try {
            LOG.info(String.format("Для задачи с ключом %s выполняется пост-обработка", lock));
            lockService.updateState(lock, lockDate, LockData.State.POST_LOGIC.getText());
            executePostLogic(params);
        } finally {
            LOG.info(String.format("Для задачи с ключом %s выполняется снятие блокировки после успешного завершения", lock));
            lockService.unlock(lock, (Integer) params.get(USER_ID.name()));
        }
        LOG.info(String.format("Для задачи с ключом %s завершено выполнение", lock));
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
        LOG.info(String.format("Для задачи с ключом %s выполняется рассылка уведомлений", lock));
        if (msg != null && !msg.isEmpty()) {
            //Получаем список пользователей-подписчиков, для которых надо сформировать оповещение
            List<Integer> waitingUsers = lockService.getUsersWaitingForLock(lock);
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
                notificationService.saveList(notifications);
            }
        }
        LOG.info(String.format("Для задачи с ключом %s закончена рассылка уведомлений", lock));
    }

    /**
     * Формирует сообщение:
     * Налоговый орган: "%s", КПП: "%s", ОКТМО: "%s", АСНУ: "%s"
     */
    public String formatDeclarationDataInfo(DeclarationData declarationData) {
        List<String> messages = new ArrayList<String>();

        if (declarationData.getTaxOrganCode() != null) {
            messages.add(String.format("Налоговый орган: \"%s\"", declarationData.getTaxOrganCode()));
        }

        if (declarationData.getKpp() != null) {
            messages.add(String.format("КПП: \"%s\"", declarationData.getKpp()));
        }

        if (declarationData.getOktmo() != null) {
            messages.add(String.format("ОКТМО: \"%s\"", declarationData.getOktmo()));
        }

        if (declarationData.getAsnuId() != null) {
            RefBookDataProvider asnuProvider = refBookFactory.getDataProvider(RefBook.Id.ASNU.getId());
            String asnuName = asnuProvider.getRecordData(declarationData.getAsnuId()).get("NAME").getStringValue();

            messages.add(String.format("АСНУ: \"%s\"", asnuName));
        }

        return StringUtils.join(messages.toArray(), ", ", null);
    }
}
