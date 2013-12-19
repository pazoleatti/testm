package com.aplana.sbrf.taxaccounting.web.module.periods.shared;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.Date;

public class OpenPeriodAction extends UnsecuredActionImpl<OpenPeriodResult> {
	TaxType taxType;
	int year;
	boolean balancePeriod;
	long departmentId;
	long dictionaryTaxPeriodId;
	Date endDate;
    boolean hasCorrectPeriod;
    Date correctPeriod;

	public TaxType getTaxType() {
		return taxType;
	}

	public void setTaxType(TaxType taxType) {
		this.taxType = taxType;
	}

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

	public long getDictionaryTaxPeriodId() {
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

    public boolean isHasCorrectPeriod() {
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
