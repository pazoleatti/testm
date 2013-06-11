package com.aplana.sbrf.taxaccounting.web.module.periods.shared;

import java.io.Serializable;

public class TableRow implements Serializable{
	private long id;
	private String periodKind;
	private String periodName;
	private boolean periodCondition;

	public long getId() {
		return id;
	}

	public void setId(long id) {
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

	public boolean isPeriodCondition() {
		return periodCondition;
	}

	public void setPeriodCondition(boolean periodCondition) {
		this.periodCondition = periodCondition;
	}
}
