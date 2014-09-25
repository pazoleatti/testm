package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.Date;

public class DepartmentReportPeriod implements Serializable{
	private static final long serialVersionUID = 5623552659772659276L;

    private Integer id;

	private ReportPeriod reportPeriod;
	
	private Integer departmentId;
	
	private boolean balance;
	
	private boolean active;

    private Date correctionDate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(Integer departmentId) {
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

    public Date getCorrectionDate() {
        return correctionDate;
    }

    public void setCorrectionDate(Date correctionDate) {
        this.correctionDate = correctionDate;
    }
}
