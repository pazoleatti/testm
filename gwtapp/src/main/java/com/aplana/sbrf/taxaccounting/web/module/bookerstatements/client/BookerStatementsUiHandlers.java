package com.aplana.sbrf.taxaccounting.web.module.bookerstatements.client;

import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.gwtplatform.mvp.client.UiHandlers;

/**
 * Хендлеры формы "Загрузка бухгалтерской отчётности"
 *
 * @author Dmitriy Levykin
 */
public interface BookerStatementsUiHandlers extends UiHandlers {
    /**
     * Перезагрузка налоговых периодов
     *
     * @param departmentId
     */
    void reloadTaxPeriods(Integer departmentId);

    /**
     * Обработка выбора налогового периода
     *
     * @param taxPeriod
     * @param departmentId
     */
    void onTaxPeriodSelected(TaxPeriod taxPeriod, Integer departmentId);
}
