package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.*;

import java.util.List;
import java.util.Map;

/**
 * Сервис для работы с оповещениями
 * @author dloshkarev
 */
public interface NotificationService {

    /**
     * Получить оповещение по его идентификатору
     * @param id идентификатор оповещения
     * @return оповещение
     */
    Notification get(long id);

    /**
     * Создает новое оповещение
     * @param notification оповещение
     * @return идентификатор нового оповещения
     */
    long save(Notification notification);

    /**
     * Создает список оповещений. В основном выполняется при назначении срока сдачи на подразделение + дочерние подразделения
     * @param notifications оповещения
     */
    void saveList(List<Notification> notifications);

    /**
     * Получает оповещение
     * @param reportPeriodId отчетный период
     * @param senderDepartmentId подразделение-отправитель
     * @param receiverDepartmentId подразделение-получатель
     * @return оповещение
     */
    Notification get(int reportPeriodId, Integer senderDepartmentId, Integer receiverDepartmentId);

    /**
     * Получает список оповещений от отправителя получателю
     *
     * @param senderDepartmentId подразделение-отправитель
     * @param receiverDepartmentId подразделение-получатель
     * @return карта оповещений с ключом по идентификатору отчетного периода
     */
    Map<Integer, Notification> mapByDepartments(int senderDepartmentId, Integer receiverDepartmentId);

	/**
	 * Получить оповещения по фильтру
	 * @param filter фильтр
	 * @return страница с оповещениями
	 */
	PagingResult<Notification> getByFilter(NotificationsFilterData filter);

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
}
