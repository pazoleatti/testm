package com.aplana.sbrf.taxaccounting.web.module.periods.shared;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class CheckPeriodStatusAction  extends UnsecuredActionImpl<CheckPeriodStatusResult> {
	TaxType taxType;
	int year;
	boolean balancePeriod;
	long departmentId;
	long dictionaryTaxPeriodId;

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

	public void setBalancePeriod(boolean balancePeriod) {
		this.balancePeriod = balancePeriod;
	}

	public boolean isBalancePeriod() {
		return balancePeriod;
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

}
