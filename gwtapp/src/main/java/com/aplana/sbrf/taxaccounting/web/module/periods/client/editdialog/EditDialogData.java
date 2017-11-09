package com.aplana.sbrf.taxaccounting.web.module.periods.client.editdialog;

import com.aplana.sbrf.taxaccounting.model.ReportPeriodViewModel;

import java.util.Date;
import java.util.List;

public class EditDialogData {
    private Integer year;
    private Integer departmentId;
    private Long dictTaxPeriodId;
    private Integer reportPeriodId;
    private List<ReportPeriodViewModel> correctionReportPeriods;
    private Date correctionDate;
    private String periodName;
    private Integer periodYear;

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

    public Integer getReportPeriodId() {
        return reportPeriodId;
    }

    public void setReportPeriodId(Integer reportPeriodId) {
        this.reportPeriodId = reportPeriodId;
    }

    public List<ReportPeriodViewModel> getCorrectionReportPeriods() {
        return correctionReportPeriods;
    }

    public void setCorrectionReportPeriods(List<ReportPeriodViewModel> correctionReportPeriods) {
        this.correctionReportPeriods = correctionReportPeriods;
    }

    public Date getCorrectionDate() {
        return correctionDate;
    }

    public void setCorrectionDate(Date correctionDate) {
        this.correctionDate = correctionDate;
    }

    public String getPeriodName() {
        return periodName;
    }

    public void setPeriodName(String periodName) {
        this.periodName = periodName;
    }

    public Integer getPeriodYear() {
        return periodYear;
    }

    public void setPeriodYear(Integer periodYear) {
        this.periodYear = periodYear;
    }
}
