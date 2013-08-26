package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

public class DepartmentReportPeriod implements Serializable{
	private static final long serialVersionUID = 5623552659772659276L;

	private ReportPeriod reportPeriod;
	
	private Long departmentId;
	
	private boolean balance;
	
	private boolean active;

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
	
}
