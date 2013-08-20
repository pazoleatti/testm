package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.gwtplatform.dispatch.shared.Result;

/**
 * @author Dmitriy Levykin
 */
public class GetUserDepartmentResult implements Result {
    // Признак контролера УНП
    // true - контролера УНП, false - контролер, null - не контролер
    private Boolean isControlUNP;

    // Подразделение текущего пользователя
    private Department department;

    public Boolean getControlUNP() {
        return isControlUNP;
    }

    public void setControlUNP(Boolean controlUNP) {
        isControlUNP = controlUNP;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }
}
