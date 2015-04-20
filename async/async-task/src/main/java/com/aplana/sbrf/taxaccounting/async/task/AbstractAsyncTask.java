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
        final String lock = (String) params.get(LOCKED_OBJECT.name());
        final Date lockDate = (Date) params.get(LOCK_DATE.name());
        log.info(String.format("Запущена асинхронная задача с ключом %s и датой окончания %s", lock, sdf.format(lockDateEnd)));
        transactionHelper.executeInNewTransaction(new TransactionLogic() {
            @Override
            public void execute() {
                try {
                    if (lockService.isLockExists(lock, lockDate)) {
                        log.info(String.format("Для задачи с ключом %s запущено выполнение бизнес-логики", lock));
                        final Logger logger = new Logger();
                        //Если блокировка на объект задачи все еще существует, значит на нем можно выполнять бизнес-логику
                        executeBusinessLogic(params, logger);
                        if (!lockService.isLockExists(lock, lockDate)) {
                            //Если после выполнения бизнес логики, оказывается, что блокировки уже нет
                            //Значит результаты нам уже не нужны - откатываем транзакцию и все изменения
                            throw new RuntimeException("Результат выполнения задачи \"" + getAsyncTaskName() + "\" больше не актуален. Выполняется откат транзакции");
                        }
                        log.info(String.format("Для задачи с ключом %s выполняется рассылка уведомлений", lock));
                        sendNotifications(lock, getNotificationMsg(params), logEntryService.save(logger.getEntries()));
                    } else {
                        throw new RuntimeException("Задача \"" + getAsyncTaskName() + "\" больше не актуальна.");
                    }
                } catch (final Exception e) {
                    log.error("Произошла ошибка при выполнении асинхронной задачи", e);
                    if (lockService.isLockExists(lock, lockDate)) {
                        transactionHelper.executeInNewTransaction(new TransactionLogic() {
                            @Override
                            public void execute() {
                                log.info(String.format("Для задачи с ключом %s выполняется рассылка уведомлений об ошибке", lock));
                                if (e instanceof ServiceLoggerException) {
                                    sendNotifications(lock, getErrorMsg(params) + ". Ошибка: " + e.getMessage(), ((ServiceLoggerException) e).getUuid());
                                } else {
                                    sendNotifications(lock, getErrorMsg(params) + ". Ошибка: " + e.getMessage(), null);
                                }
                                log.info(String.format("Для задачи с ключом %s выполняется снятие блокировки", lock));
                                lockService.unlock(lock, (Integer) params.get(USER_ID.name()));
                            }

                            @Override
                            public Object executeWithReturn() {
                                return null;
                            }
                        });
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
        log.info(String.format("Для задачи с ключом %s выполняется пост-обработка", lock));
        executePostLogic(params);
        log.info(String.format("Для задачи с ключом %s выполняется снятие блокировки после успешного завершения", lock));
        lockService.unlock(lock, (Integer) params.get(USER_ID.name()));
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
