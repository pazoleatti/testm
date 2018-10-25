package com.aplana.sbrf.taxaccounting.service;

import java.util.List;

/**
 * Сервис для Уведомлений о неудержанном налоге.
 */
public interface TaxNotificationService {

    /**
     * Добавить асинхронную задачу на создание Уведомления
     *
     * @param departmentId id тербанка
     * @param periodId     id периода
     * @param asnuIds      список id АСНУ
     * @return uuid логов создания задачи
     */
    String createAsync(Integer departmentId, Integer periodId, List<Long> asnuIds);
}
