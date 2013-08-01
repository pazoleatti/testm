package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.client;

import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.DepartmentCombined;
import com.gwtplatform.mvp.client.UiHandlers;

import java.util.Date;

/**
 * Хендлеры формы настройки подразделений
 *
 * @author Dmitriy Levykin
 */
public interface DepartmentConfigUiHandlers extends UiHandlers {
    void save(DepartmentCombined combinedDepartmentParam);
    void reloadDepartmentParams(Integer departmentId, TaxType taxType, Integer reportPeriodId);
    void reloadTaxPeriods(TaxType taxType);
    void onTaxPeriodSelected(TaxPeriod taxPeriod, Integer departmentId);
}
