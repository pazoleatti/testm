package com.aplana.sbrf.taxaccounting.dao.api;

import com.aplana.sbrf.taxaccounting.model.DepartmentPair;
import com.aplana.sbrf.taxaccounting.model.Notification;
import com.aplana.sbrf.taxaccounting.model.NotificationsFilterData;
import com.aplana.sbrf.taxaccounting.model.PagingParams;

import java.util.Date;
import java.util.List;

/**
 * Дао для работы с оповещениями
 *
 * @author dloshkarev
 */
public interface NotificationDao {

    /**
     * Возвращяет оповещение
     *
     * @param reportPeriodId       отчетный период
     * @param senderDepartmentId   подразделение-отправитель
     * @param receiverDepartmentId подразделение-получатель
     * @return оповещение или null, если ничего не найдено
     */
    Notification fetchOne(int reportPeriodId, Integer senderDepartmentId, Integer receiverDepartmentId);

    /**
     * Возвращяет оповещение по его идентификатору
     *
     * @param id идентификатор оповещения
     * @return оповещение или null, если ничего не найдено
     */
    Notification fetchOne(long id);

    /**
     * Сохраняет список уведомлений с помощью batch-запроса
     *
     * @param notifications список уведомлений
     */
    void create(List<Notification> notifications);

    /**
     * Удаляет все оповещения для группы подразделений за указанный отчетный период
     *
     * @param reportPeriodId отчетный период
     * @param departments    группа подразделений, связки подразделение-родительское подразделение
     */
    void delete(int reportPeriodId, List<DepartmentPair> departments);

    /**
     * Удалить оповещения для отчетного периода
     *
     * @param reportPeriodId отчетный период
     */
    void deleteByReportPeriod(int reportPeriodId);

    /**
     * Удаляет все оповещения из списка
     *
     * @param notificationIds идентификаторы оповещений
     */
    void deleteAll(List<Long> notificationIds);

    /**
     * Получить список оповещений по фильтру (без пагинации)
     *
     * @param filter фильтр
     * @return список идентификаторов оповещений
     */
    List<Notification> fetchAllByFilter(NotificationsFilterData filter);

    /**
     * Получить список оповещений по фильтру (с пагинацией)
     *
     * @param filter       фильтр
     * @param pagingParams параметры пагинации
     * @return список идентификаторов оповещений
     */
    List<Notification> fetchAllByFilterAndPaging(NotificationsFilterData filter, PagingParams pagingParams);

    /**
     * Получить количество оповещений по фильтру
     *
     * @param filter фильтр
     * @return количество оповещений
     */
    int fetchCountByFilter(NotificationsFilterData filter);

    /**
     * Обновляет статус уведомлений пользователя на "Просмотрен"
     *
     * @param filter фильтр оповещений
     */
    void updateReadTrueByFilter(NotificationsFilterData filter);

    /**
     * Возвращяет дату последнего оповещения
     */
    Date fetchLastNotificationDate();
}
