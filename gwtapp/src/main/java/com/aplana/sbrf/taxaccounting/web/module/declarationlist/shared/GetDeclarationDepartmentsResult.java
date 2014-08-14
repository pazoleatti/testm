package com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;
import java.util.Set;

public class GetDeclarationDepartmentsResult implements Result {
    private static final long serialVersionUID = -4125991367421388088L;

    private List<Department> departments;
    Set<Integer> departmentIds;

    public List<Department> getDepartments() {
        return departments;
    }

    public void setDepartments(List<Department> departments) {
        this.departments = departments;
    }

    public Set<Integer> getDepartmentIds() {
        return departmentIds;
    }

    public void setDepartmentIds(Set<Integer> departmentIds) {
        this.departmentIds = departmentIds;
    }
}
