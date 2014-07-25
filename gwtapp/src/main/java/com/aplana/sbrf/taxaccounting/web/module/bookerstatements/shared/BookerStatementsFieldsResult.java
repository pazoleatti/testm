package com.aplana.sbrf.taxaccounting.web.module.bookerstatements.shared;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;
import java.util.Set;

/**
 * @author lhaziev
 */
public class BookerStatementsFieldsResult implements Result {
    List<Department> departments;
    Set<Integer> departmentIds;
    Integer year;

    public Set<Integer> getDepartmentIds() {
        return departmentIds;
    }

    public void setDepartmentIds(Set<Integer> departmentIds) {
        this.departmentIds = departmentIds;
    }

    public List<Department> getDepartments() {
        return departments;
    }

    public void setDepartments(List<Department> departments) {
        this.departments = departments;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }
}
