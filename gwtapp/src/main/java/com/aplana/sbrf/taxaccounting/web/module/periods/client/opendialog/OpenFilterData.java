package com.aplana.sbrf.taxaccounting.web.module.periods.client.opendialog;

import java.util.Date;

public class OpenFilterData {
	int year;
	boolean balancePeriod;
	long departmentId;
	long dictionaryTaxPeriodId;
	Date endDate;

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public boolean isBalancePeriod() {
		return balancePeriod;
	}

	public void setBalancePeriod(boolean balancePeriod) {
		this.balancePeriod = balancePeriod;
	}

	public long getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(long departmentId) {
		this.departmentId = departmentId;
	}

	public long getDictionaryTaxPeriod() {
		return dictionaryTaxPeriodId;
	}

	public void setDictionaryTaxPeriodId(long dictionaryTaxPeriodId) {
		this.dictionaryTaxPeriodId = dictionaryTaxPeriodId;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
}
