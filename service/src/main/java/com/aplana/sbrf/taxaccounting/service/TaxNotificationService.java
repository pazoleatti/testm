package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAsnu;

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
     * @param userInfo     вызывающий пользователь
     * @return uuid логов создания задачи
     */
    String createAsync(Integer departmentId, Integer periodId, List<Long> asnuIds, TAUserInfo userInfo);

    /**
     * Создание архива с Уведомлениями по неудержанному налогу.
     *
     * @param notHoldingTaxKnf КНФ по неудержанному налогу
     * @param selectedAsnuList         список АСНУ, выбранных пользователем для генерации Уведомлений только по ним
     * @param logger           логи для нижней панели сообщений
     * @return uuid файла архива
     */
    String create(DeclarationData notHoldingTaxKnf, List<RefBookAsnu> selectedAsnuList, Logger logger);
}
