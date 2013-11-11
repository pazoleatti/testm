package com.aplana.sbrf.taxaccounting.web.module.periods.shared;

import com.aplana.sbrf.taxaccounting.model.DepartmentPair;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.Date;
import java.util.List;

/**
 * Установка срока сдачи отчетности
 * @author dloshkarev
 */
public class UpdateDepartmentDeadlineAction extends UnsecuredActionImpl<UpdateDepartmentDeadlineResult> {
    private int reportPeriodId;
    private List<DepartmentPair> departments;
    private Date deadline;
    private TaxType taxType;
    private String reportPeriodName;
    private int currentYear;

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public List<DepartmentPair> getDepartments() {
        return departments;
    }

    public void setDepartments(List<DepartmentPair> departments) {
        this.departments = departments;
    }

    public int getReportPeriodId() {
        return reportPeriodId;
    }

    public void setReportPeriodId(int reportPeriodId) {
        this.reportPeriodId = reportPeriodId;
    }

    public TaxType getTaxType() {
        return taxType;
    }

    public void setTaxType(TaxType taxType) {
        this.taxType = taxType;
    }

    public int getCurrentYear() {
        return currentYear;
    }

    public void setCurrentYear(int currentYear) {
        this.currentYear = currentYear;
    }

    public String getReportPeriodName() {
        return reportPeriodName;
    }

    public void setReportPeriodName(String reportPeriodName) {
        this.reportPeriodName = reportPeriodName;
    }
}
