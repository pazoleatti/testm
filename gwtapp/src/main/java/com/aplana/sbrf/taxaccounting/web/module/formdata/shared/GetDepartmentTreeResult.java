package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;
import java.util.Set;

public class GetDepartmentTreeResult implements Result {
    private List<Department> departments;
    private Set<Integer> availableDepartments;

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
}
