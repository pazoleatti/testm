package com.aplana.sbrf.taxaccounting.dao.api;

import com.aplana.sbrf.taxaccounting.model.DepartmentPair;
import com.aplana.sbrf.taxaccounting.model.Notification;
import com.aplana.sbrf.taxaccounting.model.NotificationsFilterData;
import com.aplana.sbrf.taxaccounting.model.PagingParams;

import java.util.List;


/**
 * Дао для работы с оповещениями
 */
public interface NotificationDao {

    /**
     * Сохраняет список уведомлений с помощью batch-запроса
     *
     * @param notifications список уведомлений
     */
    void create(List<Notification> notifications);

    /**
     * Возвращяет оповещение по его идентификатору
     *
     * @param id идентификатор оповещения
     * @return оповещение или null, если ничего не найдено
     */
    Notification findById(long id);

    /**
     * Список оповещений по списку идентификаторов.
     */
    List<Notification> findByIdIn(List<Long> ids);

    /**
     * Получить список оповещений по фильтру (с пагинацией)
     *
     * @param filter       фильтр
     * @param pagingParams параметры пагинации
     * @return список идентификаторов оповещений
     */
    List<Notification> findByFilter(NotificationsFilterData filter, PagingParams pagingParams);

    /**
     * Возвращяет оповещение
     *
     * @param reportPeriodId       отчетный период
     * @param senderDepartmentId   подразделение-отправитель
     * @param receiverDepartmentId подразделение-получатель
     * @return оповещение или null, если ничего не найдено
     */
    Notification findByReportPeriodAndDepartments(int reportPeriodId, Integer senderDepartmentId, Integer receiverDepartmentId);

    /**
     * Удаляет все оповещения для группы подразделений за указанный отчетный период
     *
     * @param reportPeriodId отчетный период
     * @param departments    группа подразделений, связки подразделение-родительское подразделение
     */
    void deleteByReportPeriodAndDepartments(int reportPeriodId, List<DepartmentPair> departments);

    /**
     * Удалить оповещения для отчетного периода
     *
     * @param reportPeriodId отчетный период
     */
    void deleteByReportPeriod(int reportPeriodId);

    /**
     * Удаляет все оповещения из списка
     *
     * @param ids идентификаторы оповещений
     */
    void deleteByIdIn(List<Long> ids);

    /**
     * Получить количество оповещений по фильтру
     *
     * @param filter фильтр
     * @return количество оповещений
     */
    int countByFilter(NotificationsFilterData filter);

    /**
     * Обновляет статус уведомлений пользователя на "Просмотрен"
     *
     * @param filter фильтр оповещений
     */
    void setReadTrueByFilter(NotificationsFilterData filter);

    /**
     * Проверяет существование записи с определенныи блобом для пользователя. Используется для того чтобы определить может ли пользователь скачать данные блоба.
     *
     * @param userId идентификатор пользователя
     * @param blobId идентификатор таблицы хранящей данные файла
     * @return возвращает {@code true} если такая запись существует, иначе возвращает {@code false}
     */
    boolean existsByUserIdAndReportId(int userId, String blobId);
}
