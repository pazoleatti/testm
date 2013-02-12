package com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared;


import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class GetDeclarationFilterDataResult implements Result {

	private List<Department> departments;
	private List<TaxPeriod> taxPeriods;

	public List<Department> getDepartments() {
		return departments;
	}

	public void setDepartments(List<Department> departments) {
		this.departments = departments;
	}

	public List<TaxPeriod> getTaxPeriods() {
		return taxPeriods;
	}

	public void setTaxPeriods(List<TaxPeriod> taxPeriods) {
		this.taxPeriods = taxPeriods;
	}
}
