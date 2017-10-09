package com.aplana.sbrf.taxaccounting.web.module.periods.shared;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.Date;

public class OpenPeriodAction extends UnsecuredActionImpl<OpenPeriodResult> {
	TaxType taxType;
	int year;
	int departmentId;
	long dictionaryTaxPeriodId;
	Date endDate;
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

	public int getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(int departmentId) {
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

    public Date getCorrectPeriod() {
        return correctPeriod;
    }

    public void setCorrectPeriod(Date correctPeriod) {
        this.correctPeriod = correctPeriod;
    }
}
