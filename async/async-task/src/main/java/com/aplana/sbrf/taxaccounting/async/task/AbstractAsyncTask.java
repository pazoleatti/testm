package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.Notification;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.NotificationService;
import com.aplana.sbrf.taxaccounting.util.TransactionHelper;
import com.aplana.sbrf.taxaccounting.util.TransactionLogic;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Абстрактная реализация асинхронной задачи.
 * В ней реализована общая часть логики взаимодействия с блокировками объектов, для которых выполняется бизнес-логика конкретных задач
 * @author dloshkarev
 */
public abstract class AbstractAsyncTask implements AsyncTask {

    protected static final Log log = LogFactory.getLog(AbstractAsyncTask.class);

    @Autowired
    private LockDataService lockService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private TransactionHelper transactionHelper;
    @Autowired
    private LogEntryService logEntryService;

    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    /**
     * Выполнение бизнес логики задачи
     * @param params параметры
     */
    protected abstract void executeBusinessLogic(Map<String, Object> params, Logger logger) throws InterruptedException;

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
    protected abstract String getErrorMsg(Map<String, Object> params);

    @Override
    public void execute(final Map<String, Object> params) {
        final String lock = (String) params.get(LOCKED_OBJECT.name());
        final Date lockDate = (Date) params.get(LOCK_DATE.name());
        final Logger logger = new Logger();
        log.info(String.format("Запущена асинхронная задача с ключом %s и датой начала %s (%s)", lock, sdf.format(lockDate), lockDate.getTime()));
        lockService.updateState(lock, lockDate, LockData.State.STARTED.getText());
        transactionHelper.executeInNewTransaction(new TransactionLogic() {
            @Override
            public void execute() {
                try {
                    if (lockService.isLockExists(lock, lockDate)) {
                        log.info(String.format("Для задачи с ключом %s запущено выполнение бизнес-логики", lock));
                        lockService.updateState(lock, lockDate, getBusinessLogicTitle());
                        //Если блокировка на объект задачи все еще существует, значит на нем можно выполнять бизнес-логику
                        executeBusinessLogic(params, logger);
                        if (!lockService.isLockExists(lock, lockDate)) {
                            //Если после выполнения бизнес логики, оказывается, что блокировки уже нет
                            //Значит результаты нам уже не нужны - откатываем транзакцию и все изменения
                            throw new RuntimeException(String.format("Результат выполнения задачи %s больше не актуален. Выполняется откат транзакции", lock));
                        }

                        transactionHelper.executeInNewTransaction(new TransactionLogic() {
                            @Override
                            public void execute() {
                                try {
                                    log.info(String.format("Для задачи с ключом %s выполняется сохранение сообщений", lock));
                                    lockService.updateState(lock, lockDate, LockData.State.SAVING_MSGS.getText());
                                    String msg = getNotificationMsg(params);
                                    logger.getEntries().add(0, new LogEntry(LogLevel.INFO, msg));
                                    String uuid = logEntryService.save(logger.getEntries());
                                    log.info(String.format("Для задачи с ключом %s выполняется рассылка уведомлений", lock));
                                    lockService.updateState(lock, lockDate, LockData.State.SENDING_MSGS.getText());
                                    sendNotifications(lock, msg, uuid);
                                } catch (Exception e) {
                                    log.error("Произошла ошибка при рассылке сообщений", e);
                                }
                            }

                            @Override
                            public Object executeWithReturn() {
                                return null;
                            }
                        });
                    } else {
                        throw new RuntimeException(String.format("Задача %s больше не актуальна.", lock));
                    }
                } catch (final Exception e) {
                    try {
                        log.error(String.format("Произошла ошибка при выполнении асинхронной задачи с ключом %s и датой начала %s (%s)",
                                lock, sdf.format(lockDate), lockDate.getTime()), e);
                        if (lockService.isLockExists(lock, lockDate)) {
                            transactionHelper.executeInNewTransaction(new TransactionLogic() {
                                @Override
                                public void execute() {
                                    log.info(String.format("Для задачи с ключом %s выполняется рассылка уведомлений об ошибке", lock));
                                    lockService.updateState(lock, lockDate, LockData.State.SENDING_ERROR_MSGS.getText());
                                    String msg = getErrorMsg(params);
                                    if (e instanceof ServiceLoggerException && ((ServiceLoggerException) e).getUuid() != null) {
                                        Logger logger1 = new Logger();
                                        logger1.error(msg);
                                        if (e.getMessage() != null && !e.getMessage().isEmpty()) logger1.error(e);
                                        sendNotifications(lock, msg, logEntryService.addFirst(logger1.getEntries(), ((ServiceLoggerException) e).getUuid()));
                                    } else {
                                        logger.getEntries().add(0, new LogEntry(LogLevel.ERROR, msg));
                                        if (e.getMessage() != null && !e.getMessage().isEmpty()) logger.error(e);
                                        sendNotifications(lock, msg, logEntryService.save(logger.getEntries()));
                                    }
                                }

                                @Override
                                public Object executeWithReturn() {
                                    return null;
                                }
                            });
                        }
                    } finally {
                        log.info(String.format("Для задачи с ключом %s выполняется снятие блокировки", lock));
                        lockService.unlock(lock, (Integer) params.get(USER_ID.name()));
                    }
                    log.info(String.format("Для задачи с ключом %s выполняется откат транзакции", lock));
                    if (e instanceof ServiceLoggerException) {
                        throw new ServiceLoggerException("Не удалось выполнить асинхронную задачу", ((ServiceLoggerException) e).getUuid());
                    } else {
                        throw new RuntimeException("Не удалось выполнить асинхронную задачу", e);
                    }
                }
            }

            @Override
            public Object executeWithReturn() {
                return null;
            }
        });

        try {
            log.info(String.format("Для задачи с ключом %s выполняется пост-обработка", lock));
            lockService.updateState(lock, lockDate, LockData.State.POST_LOGIC.getText());
            executePostLogic(params);
        } finally {
            log.info(String.format("Для задачи с ключом %s выполняется снятие блокировки после успешного завершения", lock));
            lockService.unlock(lock, (Integer) params.get(USER_ID.name()));
        }
        log.info(String.format("Для задачи с ключом %s завершено выполнение", lock));
    }

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

    /**
     * Отправка уведомлений подисчикам на указанную блокировку
     * @param lock ключ блокировки
     */
    private void sendNotifications(String lock, String msg, String uuid) {
        log.info(String.format("Для задачи с ключом %s выполняется рассылка уведомлений", lock));
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
                    notification.setBlobDataId(uuid);
                    notifications.add(notification);
                }
                //Создаем оповещение для каждого пользователя из списка
                notificationService.saveList(notifications);
            }
        }
        log.info(String.format("Для задачи с ключом %s закончена рассылка уведомлений", lock));
    }
}
