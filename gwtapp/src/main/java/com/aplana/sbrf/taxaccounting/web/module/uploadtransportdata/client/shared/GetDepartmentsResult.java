package com.aplana.sbrf.taxaccounting.web.module.uploadtransportdata.client.shared;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;
import java.util.Set;

/**
 * @author Dmitriy Levykin
 */
public class GetDepartmentsResult implements Result {
    private List<Department> departments;
    private Set<Integer> availableDepartments;
    private Integer defaultDepartmentId;
    private boolean canChooseDepartment = false;

    public List<Department> getDepartments() {
        return departments;
    }

    public void setDepartments(List<Department> departments) {
        this.departments = departments;
    }

    public Set<Integer> getAvailableDepartments() {
        return availableDepartments;
    }

    public void setAvailableDepartments(Set<Integer> availableDepartments) {
        this.availableDepartments = availableDepartments;
    }

    public Integer getDefaultDepartmentId() {
        return defaultDepartmentId;
    }

    public void setDefaultDepartmentId(Integer defaultDepartmentId) {
        this.defaultDepartmentId = defaultDepartmentId;
    }

    public boolean isCanChooseDepartment() {
        return canChooseDepartment;
    }

    public void setCanChooseDepartment(boolean canChooseDepartment) {
        this.canChooseDepartment = canChooseDepartment;
    }
}
