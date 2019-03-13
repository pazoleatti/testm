package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.*;

import java.util.List;

/**
 * Сервис для работы с оповещениями
 */
public interface NotificationService {

    /**
     * Создает список оповещений. В основном выполняется при назначении срока сдачи на подразделение + дочерние подразделения
     *
     * @param notifications оповещения
     */
    void create(List<Notification> notifications);

    /**
     * Список оповещений по списку идентификаторов.
     */
    List<Notification> findByIdIn(List<Long> ids);

    /**
     * Получить оповещения по фильтру (с пагинацией для angularJs)
     *
     * @param filter       фильтр
     * @param pagingParams параметры пагинации
     * @return страница с оповещениями
     */
    PagingResult<Notification> findByFilter(NotificationsFilterData filter, PagingParams pagingParams);

    /**
     * Получить количество оповещений по фильтру
     *
     * @param filter фильтр
     * @return количество оповещений
     */
    int countByFilter(NotificationsFilterData filter);

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
    void setReadTrueByFilter(NotificationsFilterData filter);

    /**
     * Удаляет все оповещения из списка
     *
     * @param notificationIds идентификаторы оповещений
     */
    void deleteByIdIn(List<Long> notificationIds);



    /**
     * Получение данных оповещения из файлового хранилища
     *
     * @param notification объект оповещения. Необходим огрызок объекта Notification с заполненными userId и reportId.
     * @return данные полученные из файлового хранилища
     */
    BlobData getNotificationBlobData(Notification notification);
}
