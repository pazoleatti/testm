package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.client;

import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.DepartmentCombined;
import com.gwtplatform.mvp.client.UiHandlers;

/**
 * Хендлеры формы настройки подразделений
 *
 * @author Dmitriy Levykin
 */
public interface DepartmentConfigUiHandlers extends UiHandlers {
    void save(DepartmentCombined combinedDepartmentParam);
    void updateDepartment(Integer next);
}
