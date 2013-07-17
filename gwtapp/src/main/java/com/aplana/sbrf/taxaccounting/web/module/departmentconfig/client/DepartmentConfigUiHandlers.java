package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.client;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.mvp.client.UiHandlers;

/**
 * Хендлеры формы настройки подразделений
 *
 * @author Dmitriy Levykin
 */
public interface DepartmentConfigUiHandlers extends UiHandlers {
    void save();
    void updateDepartment(Integer next);
    boolean onTaxTypeChange(TaxType type);
}
