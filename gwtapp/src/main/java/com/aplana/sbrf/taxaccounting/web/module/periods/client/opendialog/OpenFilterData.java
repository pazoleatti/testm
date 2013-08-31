package com.aplana.sbrf.taxaccounting.web.module.periods.client.opendialog;

import java.util.Date;

public class OpenFilterData {
	Integer year;
	boolean balancePeriod;
	Long departmentId;
	Long dictionaryTaxPeriodId;
	Date endDate;

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public boolean isBalancePeriod() {
		return balancePeriod;
	}

	public void setBalancePeriod(boolean balancePeriod) {
		this.balancePeriod = balancePeriod;
	}

	public Long getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(Long departmentId) {
		this.departmentId = departmentId;
	}

	public Long getDictionaryTaxPeriod() {
		return dictionaryTaxPeriodId;
	}

	public void setDictionaryTaxPeriodId(Long dictionaryTaxPeriodId) {
		this.dictionaryTaxPeriodId = dictionaryTaxPeriodId;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
}
