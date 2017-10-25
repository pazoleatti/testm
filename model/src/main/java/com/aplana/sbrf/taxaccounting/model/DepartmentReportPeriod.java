package com.aplana.sbrf.taxaccounting.model;

import org.joda.time.LocalDateTime;

import java.io.Serializable;

public class DepartmentReportPeriod implements Serializable{
	private static final long serialVersionUID = 5623552659772659276L;

    private Long id;

	private ReportPeriod reportPeriod;
	
	private Integer departmentId;
	
	private boolean active;

    private LocalDateTime correctionDate;

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

	public ReportPeriod getReportPeriod() {
		return reportPeriod;
	}

	public void setReportPeriod(ReportPeriod reportPeriod) {
		this.reportPeriod = reportPeriod;
	}

    public LocalDateTime getCorrectionDate() {
        return correctionDate;
    }

    public void setCorrectionDate(LocalDateTime correctionDate) {
        this.correctionDate = correctionDate;
    }
}
