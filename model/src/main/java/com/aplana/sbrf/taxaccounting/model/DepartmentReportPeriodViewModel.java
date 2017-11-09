package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.Date;


public class DepartmentReportPeriodViewModel implements Serializable {
    private static final long serialVersionUID = 5623552659772659276L;

    private Long id;

    private ReportPeriodViewModel reportPeriod;

    private Integer departmentId;

    private boolean active;

    private Date correctionDate;

    public DepartmentReportPeriodViewModel(Long id, ReportPeriodViewModel reportPeriod, Integer departmentId, boolean active, Date correctionDate) {
        this.id = id;
        this.reportPeriod = reportPeriod;
        this.departmentId = departmentId;
        this.active = active;
        this.correctionDate = correctionDate;
    }

    public DepartmentReportPeriodViewModel() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public ReportPeriodViewModel getReportPeriod() {
        return reportPeriod;
    }

    public void setReportPeriod(ReportPeriodViewModel reportPeriod) {
        this.reportPeriod = reportPeriod;
    }

    public Date getCorrectionDate() {
        return correctionDate;
    }

    public void setCorrectionDate(Date correctionDate) {
        this.correctionDate = correctionDate;
    }
}