package com.aplana.sbrf.taxaccounting.web.module.periods.client.opendialog;

import com.aplana.sbrf.taxaccounting.model.DictionaryTaxPeriod;

import java.util.Date;

public class OpenFilterData {
	int year;
	boolean balancePeriod;
	long departmentId;
	DictionaryTaxPeriod dictionaryTaxPeriod;
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

	public DictionaryTaxPeriod getDictionaryTaxPeriod() {
		return dictionaryTaxPeriod;
	}

	public void setDictionaryTaxPeriod(DictionaryTaxPeriod dictionaryTaxPeriod) {
		this.dictionaryTaxPeriod = dictionaryTaxPeriod;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
}
