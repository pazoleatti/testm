package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;
import java.util.Set;

/**
 * @author Stanislav Yasinskiy
 */
public class GetOpenDataResult implements Result {

    // Признак контролера УНП
    // true - контролера УНП, false - контролер, null - не контролер
    private Boolean isControlUNP;

    // Список всех подразделений
    private List<Department> departments;

    // Список id подразделений, доступных пользователю
    private Set<Integer> availableDepartments;

    public List<Department> getDepartments() {
        return departments;
    }

    public void setDepartments(List<Department> departments) {
        this.departments = departments;
    }

    public Boolean getControlUNP() {
        return isControlUNP;
    }

    public void setControlUNP(Boolean controlUNP) {
        isControlUNP = controlUNP;
    }

    public Set<Integer> getAvailableDepartments() {
        return availableDepartments;
    }

    public void setAvailableDepartments(Set<Integer> availableDepartments) {
        this.availableDepartments = availableDepartments;
    }
}
