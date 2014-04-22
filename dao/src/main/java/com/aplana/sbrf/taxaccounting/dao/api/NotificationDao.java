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
    int save(Notification notification);

    /**
     * Получает оповещение
     * @param reportPeriodId отчетный период
     * @param senderDepartmentId подразделение-отправитель
     * @param receiverDepartmentId подразделение-получатель
     * @return оповещение
     */
    Notification get(int reportPeriodId, int senderDepartmentId, Integer receiverDepartmentId);

    /**
     * Получает список оповещений от отправителя получателю
     * @param senderDepartmentId подразделение-отправитель
     * @param receiverDepartmentId подразделение-получатель
     * @return оповещение
     */
    List<Notification> listByDepartments(int senderDepartmentId, Integer receiverDepartmentId);

    /**
     * Сохраняет список уведомлений с помощью batch-запроса
     * @param notifications список уведомлений
     */
    void saveList(List<Notification> notifications);

    /**
     * Удаляет оповещение
     * @param reportPeriodId отчетный период
     * @param senderDepartmentId подразделение-отправитель
     * @param receiverDepartmentId подразделение-получатель
     */
    void delete(int reportPeriodId, int senderDepartmentId, Integer receiverDepartmentId);

    /**
     * Удаляет все оповещения для группы подразделений за указанный отчетный период
     * @param reportPeriodId отчетный период
     * @param departments группа подразделений, связки подразделение-родительское подразделение
     */
    void deleteList(int reportPeriodId, List<DepartmentPair> departments);

	/**
	 * Получить список идентификаторов оповещений для подразделения - получателя
	 * @param departmentId подразделение - получатель
	 * @return список идентификаторов оповещений
	 */
	List<Integer> listForDepartment(int departmentId);

	/**
	 * Получить оповещение по его идентификатору
	 * @param id идентификатор оповещения
	 * @return оповещение
	 */
	Notification get(int id);

	/**
	 * Получить список оповещений по фильтру
	 * @param filter фильтр
	 * @return список идентификаторов оповещений
	 */
	List<Integer> getByFilter(NotificationsFilterData filter);

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
}
