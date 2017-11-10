package com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;
import java.util.Set;

public class GetDeclarationDepartmentsResult implements Result {
    private static final long serialVersionUID = -4125991367421388088L;

    private List<Department> departments;
    private Set<Integer> departmentIds;
    private Integer defaultDepartmentId;
    private List<DepartmentReportPeriod> departmentReportPeriods;

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

    public Integer getDefaultDepartmentId() {
        return defaultDepartmentId;
    }

    public void setDefaultDepartmentId(Integer defaultDepartmentId) {
        this.defaultDepartmentId = defaultDepartmentId;
    }

    public List<DepartmentReportPeriod> getDepartmentReportPeriods() {
        return departmentReportPeriods;
    }

    public void setDepartmentReportPeriods(List<DepartmentReportPeriod> departmentReportPeriods) {
        this.departmentReportPeriods = departmentReportPeriods;
    }
}
