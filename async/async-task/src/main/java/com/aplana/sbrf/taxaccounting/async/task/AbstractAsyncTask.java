package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.core.api.LockDataService;
import com.aplana.sbrf.taxaccounting.model.Notification;
import com.aplana.sbrf.taxaccounting.service.NotificationService;
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

    /**
     * Выполнение бизнес логики задачи
     * @param params параметры
     */
    protected abstract void executeBusinessLogic(Map<String, Object> params);

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

    @Override
    public void execute(Map<String, Object> params) {
        String lock = (String) params.get(LOCKED_OBJECT.name());
        try {
            if (lockService.isLockExists(lock)) {
                //Если блокировка на объект задачи все еще существует, значит на нем можно выполнять бизнес-логику
                executeBusinessLogic(params);
                if (!lockService.isLockExists(lock)) {
                    //Если после выполнения бизнес логики, оказывается, что блокировки уже нет
                    //Значит результаты нам уже не нужны - откатываем транзакцию и все изменения
                    throw new RuntimeException("Результат выполнения задачи \"" + getAsyncTaskName() + "\" больше не актуален. Выполняется откат транзакции");
                }
                //Получаем список пользователей, для которых надо сформировать оповещение
                String msg = getNotificationMsg(params);
                if (msg != null && !msg.isEmpty()) {
                    List<Integer> waitingUsers = lockService.getUsersWaitingForLock(lock);
                    if (!waitingUsers.isEmpty()) {
                        List<Notification> notifications = new ArrayList<Notification>();
                        for (Integer userId : waitingUsers) {
                            Notification notification = new Notification();
                            notification.setUserId(userId);
                            notification.setCreateDate(new Date());
                            notification.setText(msg);
                            notifications.add(notification);
                        }
                        //Создаем оповещение для каждого пользователя из списка
                        notificationService.saveList(notifications);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Не удалось выполнить асинхронную задачу", e);
        } finally {
            //Снимаем блокировку
            lockService.unlock(lock, (Integer) params.get(USER_ID.name()), true);
        }
    }
}
