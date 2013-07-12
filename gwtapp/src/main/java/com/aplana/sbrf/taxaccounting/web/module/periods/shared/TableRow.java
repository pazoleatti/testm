package com.aplana.sbrf.taxaccounting.web.module.periods.shared;

import java.io.Serializable;

public class TableRow implements Serializable {

	private int id;
	private String periodKind;
	private String periodName;
	private Boolean periodCondition;
	private boolean isSubHeader;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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
}
