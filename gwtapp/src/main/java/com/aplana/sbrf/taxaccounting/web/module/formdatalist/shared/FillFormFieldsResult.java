package com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.gwtplatform.dispatch.shared.Result;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * User: avanteev
 */
public class FillFormFieldsResult implements Result {
    List<FormType> formTypes;
    List<FormDataKind> dataKinds;
    List<Department> departments;
    Set<Integer> departmentIds;
    Integer defaultDepartmentId;
    List<ReportPeriod> reportPeriods;
    ReportPeriod defaultReportPeriod;
    Date correctionDate;

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

    public ReportPeriod getDefaultReportPeriod() {
        return defaultReportPeriod;
    }

    public void setDefaultReportPeriod(ReportPeriod defaultReportPeriod) {
        this.defaultReportPeriod = defaultReportPeriod;
    }

    public List<FormDataKind> getDataKinds() {
        return dataKinds;
    }

    public void setDataKinds(List<FormDataKind> dataKinds) {
        this.dataKinds = dataKinds;
    }

    public List<Department> getDepartments() {
        return departments;
    }

    public void setDepartments(List<Department> departments) {
        this.departments = departments;
    }

    public Integer getDefaultDepartmentId() {
        return defaultDepartmentId;
    }

    public void setDefaultDepartmentId(Integer defaultDepartmentId) {
        this.defaultDepartmentId = defaultDepartmentId;
    }

    public List<FormType> getFormTypes() {
        return formTypes;
    }

    public void setFormTypes(List<FormType> formTypes) {
        this.formTypes = formTypes;
    }

    public Date getCorrectionDate() {
        return correctionDate;
    }

    public void setCorrectionDate(Date correctionDate) {
        this.correctionDate = correctionDate;
    }
}
