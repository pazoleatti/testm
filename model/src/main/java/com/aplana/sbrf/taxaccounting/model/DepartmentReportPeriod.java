package com.aplana.sbrf.taxaccounting.model;

import org.joda.time.LocalDateTime;

import java.io.Serializable;

public class DepartmentReportPeriod implements Serializable, SecuredEntity {
	private static final long serialVersionUID = 5623552659772659276L;

    private Long id;

	private ReportPeriod reportPeriod;
	
	private Integer departmentId;
	
	private boolean isActive;

    private LocalDateTime correctionDate;

	private long permissions;

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
		return isActive;
	}

	public void setIsActive(byte active) {
		this.isActive = active == 1;
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

	@Override
	public long getPermissions() {
		return permissions;
	}

	@Override
	public void setPermissions(long permissions) {
		this.permissions = permissions;
	}

	public void setActive(boolean active) {
		this.isActive = active;
	}
}
