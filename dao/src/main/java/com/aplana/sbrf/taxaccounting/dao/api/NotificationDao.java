package com.aplana.sbrf.taxaccounting.dao.api;

import com.aplana.sbrf.taxaccounting.model.DepartmentPair;
import com.aplana.sbrf.taxaccounting.model.Notification;
import com.aplana.sbrf.taxaccounting.model.NotificationsFilterData;

import java.util.List;

/**
 * Дао для работы с оповещениями
 * @author dloshkarev
 */
public interface NotificationDao {
    /**
     * Создает новое оповещение
     * @param notification оповещение
     * @return идентификатор нового оповещения
     */
    long save(Notification notification);

    /**
     * Получает оповещение
     * @param reportPeriodId отчетный период
     * @param senderDepartmentId подразделение-отправитель
     * @param receiverDepartmentId подразделение-получатель
     * @return оповещение
     */
    Notification get(int reportPeriodId, Integer senderDepartmentId, Integer receiverDepartmentId);

    /**
     * Сохраняет список уведомлений с помощью batch-запроса
     * @param notifications список уведомлений
     */
    void saveList(List<Notification> notifications);

    /**
     * Удаляет все оповещения для группы подразделений за указанный отчетный период
     * @param reportPeriodId отчетный период
     * @param departments группа подразделений, связки подразделение-родительское подразделение
     */
    void deleteList(int reportPeriodId, List<DepartmentPair> departments);

	/**
	 * Получить оповещение по его идентификатору
	 * @param id идентификатор оповещения
	 * @return оповещение
	 */
	Notification get(long id);

	/**
	 * Получить список оповещений по фильтру
	 * @param filter фильтр
	 * @return список идентификаторов оповещений
	 */
	List<Notification> getByFilter(NotificationsFilterData filter);

	/**
	 * Получить количество оповещений по фильтру
	 * @param filter фильтр
	 * @return количество оповещений
	 */
	int getCountByFilter(NotificationsFilterData filter);

    /**
     * Удалить оповещения для отчетного периода
     * @param reportPeriodId отчетный период
     */
    void deleteByReportPeriod(int reportPeriodId);

    /**
     * Обновляет статус уведомлений пользователя на "Просмотрен"
     * @param filter фильтр оповещений
     */
    void updateUserNotificationsStatus(NotificationsFilterData filter);

    /**
     * Удаляет все оповещения из списка
     * @param notificationIds идентификаторы оповещений
     */
    void deleteAll(List<Long> notificationIds);

    /**
     * Проверяет на какие оповещения из списка у пользователя есть полные права
     * @param notificationIds идентификаторы оповещений
     * @param userId идентификатор пользователя
     * @return список оповещений, на которые у пользователя есть полные права
     */
    List<Long> getAllowedNotifications(List<Long> notificationIds, int userId);
}
