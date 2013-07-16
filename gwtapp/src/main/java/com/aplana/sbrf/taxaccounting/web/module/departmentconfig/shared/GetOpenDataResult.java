package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentCombined;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

/**
 * @author Dmitriy Levykin
 */
public class GetOpenDataResult implements Result {

    // Признак контролера УНП
    // true - контролера УНП, false - контролер, null - не контролер
    Boolean isControlUNP;

    // Список подразделений, доступных пользователю
    private List<Department> departments;

    // Подразделение текущего пользователя
    Department department;

    public List<Department> getDepartments() {
        return departments;
    }

    public void setDepartments(List<Department> departments) {
        this.departments = departments;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Boolean getControlUNP() {
        return isControlUNP;
    }

    public void setControlUNP(Boolean controlUNP) {
        isControlUNP = controlUNP;
    }
}
