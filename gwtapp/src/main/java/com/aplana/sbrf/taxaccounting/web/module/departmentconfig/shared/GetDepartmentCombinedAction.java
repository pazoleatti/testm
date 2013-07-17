package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 *   Action формы настроек подразделений
 *   @author Dmitriy Levykin
 */
public class GetDepartmentCombinedAction extends UnsecuredActionImpl<GetDepartmentCombinedResult> implements ActionName {

    // Код подразделения
    private Integer departmentId;

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    @Override
    public String getName() {
        return "Получение деталей подразделения";
    }
}
