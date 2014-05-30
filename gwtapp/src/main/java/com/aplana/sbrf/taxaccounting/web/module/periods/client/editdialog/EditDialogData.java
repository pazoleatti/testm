package com.aplana.sbrf.taxaccounting.web.module.periods.client.editdialog;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;

import java.util.Date;
import java.util.List;

public class EditDialogData {
    private boolean isBalance;
    private Integer year;
    private Integer departmentId;
    private Long dictTaxPeriodId;
    private Long reportPeriodId;
    private List<ReportPeriod> correctionReportPeriods;
    private Date correctionDate;

    public boolean isBalance() {
        return isBalance;
    }

    public void setBalance(boolean isBalance) {
        this.isBalance = isBalance;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public Long getDictTaxPeriodId() {
        return dictTaxPeriodId;
    }

    public void setDictTaxPeriodId(Long dictTaxPeriodId) {
        this.dictTaxPeriodId = dictTaxPeriodId;
    }

    public Long getReportPeriodId() {
        return reportPeriodId;
    }

    public void setReportPeriodId(Long reportPeriodId) {
        this.reportPeriodId = reportPeriodId;
    }

    public List<ReportPeriod> getCorrectionReportPeriods() {
        return correctionReportPeriods;
    }

    public void setCorrectionReportPeriods(List<ReportPeriod> correctionReportPeriods) {
        this.correctionReportPeriods = correctionReportPeriods;
    }

    public Date getCorrectionDate() {
        return correctionDate;
    }

    public void setCorrectionDate(Date correctionDate) {
        this.correctionDate = correctionDate;
    }
}
