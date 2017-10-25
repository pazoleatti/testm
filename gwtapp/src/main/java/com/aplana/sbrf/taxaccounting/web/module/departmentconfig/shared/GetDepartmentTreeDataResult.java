package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.ReportPeriodViewModel;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;
import java.util.Set;

/**
 * @author Dmitriy Levykin
 */
public class GetDepartmentTreeDataResult implements Result {

    // Список всех подразделений
    private List<Department> departments;

    // Список id подразделений, доступных пользователю
    private Set<Integer> availableDepartments;

    // Список отчетных периодов
    private List<ReportPeriodViewModel> reportPeriods;

    // Подразделение установленое по умолчанию
    private Department department;

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

    public List<ReportPeriodViewModel> getReportPeriods() {
        return reportPeriods;
    }

    public void setReportPeriods(List<ReportPeriodViewModel> reportPeriods) {
        this.reportPeriods = reportPeriods;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }
}
