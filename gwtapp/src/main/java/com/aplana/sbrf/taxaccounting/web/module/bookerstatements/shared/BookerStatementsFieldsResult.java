package com.aplana.sbrf.taxaccounting.web.module.bookerstatements.shared;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;
import java.util.Set;

/**
 * @author lhaziev
 */
public class BookerStatementsFieldsResult implements Result {
    List<Department> departments;
    Set<Integer> departmentIds;
    List<ReportPeriod> reportPeriods;

    public Set<Integer> getDepartmentIds() {
        return departmentIds;
    }

    public void setDepartmentIds(Set<Integer> departmentIds) {
        this.departmentIds = departmentIds;
    }

    public List<ReportPeriod> getReportPeriods() {
        return reportPeriods;
    }

    public void setReportPeriods(List<ReportPeriod> reportPeriods) {
        this.reportPeriods = reportPeriods;
    }

    public List<Department> getDepartments() {
        return departments;
    }

    public void setDepartments(List<Department> departments) {
        this.departments = departments;
    }
}
