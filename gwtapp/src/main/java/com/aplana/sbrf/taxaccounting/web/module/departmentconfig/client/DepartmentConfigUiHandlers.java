package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.client;

import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.DepartmentCombined;
import com.gwtplatform.mvp.client.UiHandlers;

/**
 * Хендлеры формы настройки подразделений
 *
 * @author Dmitriy Levykin
 */
public interface DepartmentConfigUiHandlers extends UiHandlers {
    /**
     * Сохранение
     * @param combinedDepartmentParam
     */
    void save(DepartmentCombined combinedDepartmentParam);

    /**
     * Очистка формы
     */
    void clear();

    /**
     * Перезагрузка параметров подразделений на форме
     * @param departmentId
     * @param taxType
     * @param reportPeriodId
     */
    void reloadDepartmentParams(Integer departmentId, TaxType taxType, Integer reportPeriodId);

    /**
     * Перезагрузка налоговых периодов
     * @param taxType
     * @param departmentId
     */
    void reloadTaxPeriods(TaxType taxType, Integer departmentId);

    /**
     * Обработка выбора налогового периода
     * @param taxPeriod
     * @param departmentId
     */
    void onTaxPeriodSelected(TaxPeriod taxPeriod, Integer departmentId);
}
