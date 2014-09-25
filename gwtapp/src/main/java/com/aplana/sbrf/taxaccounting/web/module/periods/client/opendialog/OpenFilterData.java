package com.aplana.sbrf.taxaccounting.web.module.periods.client.opendialog;

import java.util.Date;

public class OpenFilterData {
	Integer year;
	boolean balancePeriod;
	Integer departmentId;
	Long dictionaryTaxPeriodId;
	Date endDate;
    Date correctPeriod;

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

	public Integer getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(Integer departmentId) {
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

    public Date getCorrectPeriod() {
        return correctPeriod;
    }

    public void setCorrectPeriod(Date correctPeriod) {
        this.correctPeriod = correctPeriod;
    }
}
