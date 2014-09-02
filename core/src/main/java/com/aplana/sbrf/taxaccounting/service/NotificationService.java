package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.Notification;
import com.aplana.sbrf.taxaccounting.model.NotificationsFilterData;
import com.aplana.sbrf.taxaccounting.model.PagingResult;

import java.util.List;
import java.util.Map;

/**
 * Сервис для работы с оповещениями
 * @author dloshkarev
 */
public interface NotificationService {

    /**
     * Создает новое оповещение
     * @param notification оповещение
     * @return идентификатор нового оповещения
     */
    int save(Notification notification);

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
    Notification get(int reportPeriodId, int senderDepartmentId, Integer receiverDepartmentId);

    /**
     * Получает список оповещений от отправителя получателю
     *
     * @param senderDepartmentId подразделение-отправитель
     * @param receiverDepartmentId подразделение-получатель
     * @return карта оповещений с ключом по идентификатору отчетного периода
     */
    Map<Integer, Notification> mapByDepartments(int senderDepartmentId, Integer receiverDepartmentId);

	/**
	 * Получить список оповещений для подразделения - получателя
	 * @param departmentId подразделение - получатель
	 * @return список оповещений
	 */
	List<Notification> notificationsForDepartment(int departmentId);

	/**
	 * Получить страницу с оповещениями
	 * @param filter фильтр
	 * @return страница с оповещениями
	 */
	PagingResult<Notification> getByFilter(NotificationsFilterData filter);

	/**
	 * Получить количество оповещений для подразделения - получателя
	 * @param receiverDepartmentId идентификатор подразделения - получателя
	 * @return количество оповещений
	 */
	int getCount(int receiverDepartmentId);

    /**
     * Удалить оповещения для отчетного периода
     * @param reportPeriodId отчетный период
     */
    void deleteByReportPeriod(int reportPeriodId);
}
