package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.Notification;
import com.aplana.sbrf.taxaccounting.model.NotificationsFilterData;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;

import java.util.Date;
import java.util.List;

/**
 * Сервис для работы с оповещениями
 *
 * @author dloshkarev
 */
public interface NotificationService {

    /**
     * Возвращяет оповещение по его идентификатору
     *
     * @param id идентификатор оповещения
     * @return оповещение или null, если ничего не найдено
     */
    Notification fetchOne(long id);

    /**
     * Создает список оповещений. В основном выполняется при назначении срока сдачи на подразделение + дочерние подразделения
     *
     * @param notifications оповещения
     */
    void create(List<Notification> notifications);

    /**
     * Получает оповещение
     *
     * @param reportPeriodId       отчетный период
     * @param senderDepartmentId   подразделение-отправитель
     * @param receiverDepartmentId подразделение-получатель
     * @return оповещение или null, если ничего не найдено
     */
    Notification fetchOne(int reportPeriodId, Integer senderDepartmentId, Integer receiverDepartmentId);

    /**
     * Получить оповещения по фильтру (без пагинации)
     *
     * @param filter фильтр
     * @return страница с оповещениями
     */
    PagingResult<Notification> fetchByFilter(NotificationsFilterData filter);

    /**
     * Получить оповещения по фильтру (с пагинацией для angularJs)
     *
     * @param filter       фильтр
     * @param pagingParams параметры пагинации
     * @return страница с оповещениями
     */
    PagingResult<Notification> fetchAllByFilterAndPaging(NotificationsFilterData filter, PagingParams pagingParams);

    /**
     * Получить количество оповещений по фильтру
     *
     * @param filter фильтр
     * @return количество оповещений
     */
    int fetchCountByFilter(NotificationsFilterData filter);

    /**
     * Удалить оповещения для отчетного периода
     *
     * @param reportPeriodId отчетный период
     */
    void deleteByReportPeriod(int reportPeriodId);

    /**
     * Обновляет статус уведомлений пользователя на "Просмотрен"
     *
     * @param filter фильтр оповещений
     */
    void updateReadTrueByFilter(NotificationsFilterData filter);

    /**
     * Удаляет все оповещения из списка
     *
     * @param notificationIds идентификаторы оповещений
     */
    void deleteAll(List<Long> notificationIds);

    /**
     * Возвращяет дату последнего оповещения
     */
    Date fetchLastNotificationDate();
}
