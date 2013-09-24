package com.aplana.sbrf.taxaccounting.web.module.periods.shared;

import java.io.Serializable;

public class TableRow implements Serializable {

	private int id;
	private long reportPeriodId;
	private long departmentId;
	private String periodKind;
	private String periodName;
	private Boolean periodCondition;
	private boolean isSubHeader;
    private Boolean balance;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getReportPeriodId() {
		return reportPeriodId;
	}

	public void setReportPeriodId(long reportPeriodId) {
		this.reportPeriodId = reportPeriodId;
	}

	public long getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(long departmentId) {
		this.departmentId = departmentId;
	}

	public String getPeriodKind() {
		return periodKind;
	}

	public void setPeriodKind(String periodKind) {
		this.periodKind = periodKind;
	}

	public String getPeriodName() {
		return periodName;
	}

	public void setPeriodName(String periodName) {
		this.periodName = periodName;
	}

	public Boolean isOpen() {
		return periodCondition;
	}

	public void setPeriodCondition(Boolean periodCondition) {
		this.periodCondition = periodCondition;
	}

	public boolean isSubHeader() {
		return isSubHeader;
	}

	public void setSubHeader(boolean subHeader) {
		isSubHeader = subHeader;
	}

    public Boolean isBalance() {
        return balance;
    }

    public void setBalance(Boolean balance) {
        this.balance = balance;
    }
}
