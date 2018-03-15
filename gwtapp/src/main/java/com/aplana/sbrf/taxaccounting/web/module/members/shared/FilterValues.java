package com.aplana.sbrf.taxaccounting.web.module.members.shared;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;
import java.util.Set;

public class FilterValues implements Result {

    private List<TARole> roles;
    private List<Department> departments;
    private String roleFilter;
    private boolean canEdit = false;

    public List<TARole> getRoles() {
        return roles;
    }

    public void setRoles(List<TARole> roles) {
        this.roles = roles;
    }

    public List<Department> getDepartments() {
        return departments;
    }

    public void setDepartments(List<Department> departments) {
        this.departments = departments;
    }

    public String getRoleFilter() {
        return roleFilter;
    }

    public void setRoleFilter(String roleFilter) {
        this.roleFilter = roleFilter;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }
}
