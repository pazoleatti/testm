package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.Notification;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.NotificationService;
import com.aplana.sbrf.taxaccounting.util.TransactionHelper;
import com.aplana.sbrf.taxaccounting.util.TransactionLogic;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import static com.aplana.sbrf.taxaccounting.async.task.AsyncTask.RequiredParams.*;

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
    protected final Log log = LogFactory.getLog(getClass());

    @Autowired
    private LockDataService lockService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private TransactionHelper transactionHelper;
    @Autowired
    private LogEntryService logEntryService;

    /**
     * Выполнение бизнес логики задачи
     * @param params параметры
     */
    protected abstract void executeBusinessLogic(Map<String, Object> params, Logger logger);

    /**
     * Возвращает название задачи. Используется при выводе ошибок.
     * @return название задачи
     */
    protected abstract String getAsyncTaskName();

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
        log.debug("AbstractAsyncTask has been started");
        final String lock = (String) params.get(LOCKED_OBJECT.name());
        Date lockDateEnd = (Date) params.get(LOCK_DATE_END.name());
        try {
            if (lockService.isLockExists(lock, lockDateEnd)) {
                log.debug("Async task lock exists");
                Logger logger = new Logger();
                //Если блокировка на объект задачи все еще существует, значит на нем можно выполнять бизнес-логику
                executeBusinessLogic(params, logger);
                if (!lockService.isLockExists(lock, lockDateEnd)) {
                    //Если после выполнения бизнес логики, оказывается, что блокировки уже нет
                    //Значит результаты нам уже не нужны - откатываем транзакцию и все изменения
                    throw new RuntimeException("Результат выполнения задачи \"" + getAsyncTaskName() + "\" больше не актуален. Выполняется откат транзакции");
                }
                sendNotifications(lock, getNotificationMsg(params), logEntryService.save(logger.getEntries()));
                lockService.unlock(lock, (Integer) params.get(USER_ID.name()));
            }
        } catch (final Exception e) {
            log.error(e, e);
            if (lockService.isLockExists(lock, lockDateEnd)) {
                transactionHelper.executeInNewTransaction(new TransactionLogic() {
                    @Override
                    public void execute() {
                        if (e instanceof ServiceLoggerException) {
                            sendNotifications(lock, getErrorMsg(params) + ". Ошибка: " + e.getMessage(), ((ServiceLoggerException) e).getUuid());
                        } else {
                            sendNotifications(lock, getErrorMsg(params) + ". Ошибка: " + e.getMessage(), null);
                        }
                        lockService.unlock(lock, (Integer) params.get(USER_ID.name()));
                    }

                    @Override
                    public Object executeWithReturn() {
                        return null;
                    }
                });
            }
            throw new RuntimeException("Не удалось выполнить асинхронную задачу", e);
        }
    }

    /**
     * Отправка уведомлений подисчикам на указанную блокировку
     * @param lock ключ блокировки
     */
    private void sendNotifications(String lock, String msg, String uuid) {
        log.debug("Sending notification has been started");
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
        log.debug("Sending notification has been finished");
    }
}
