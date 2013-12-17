package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.Date;

public class DepartmentReportPeriod implements Serializable{
	private static final long serialVersionUID = 5623552659772659276L;

	private ReportPeriod reportPeriod;
	
	private Long departmentId;
	
	private boolean balance;
	
	private boolean active;

    private boolean hasCorrectPeriod;

    private Date correctPeriod;

	public Long getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(Long departmentId) {
		this.departmentId = departmentId;
	}

	public boolean isBalance() {
		return balance;
	}

	public void setBalance(boolean balance) {
		this.balance = balance;
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

    public boolean hasCorrectPeriod() {
        return hasCorrectPeriod;
    }

    public void setHasCorrectPeriod(boolean hasCorrectPeriod) {
        this.hasCorrectPeriod = hasCorrectPeriod;
    }

    public Date getCorrectPeriod() {
        return correctPeriod;
    }

    public void setCorrectPeriod(Date correctPeriod) {
        this.correctPeriod = correctPeriod;
    }
}
