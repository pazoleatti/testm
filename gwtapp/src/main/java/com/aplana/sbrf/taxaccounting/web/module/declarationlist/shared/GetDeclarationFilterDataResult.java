package com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared;


import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormDataFilterAvailableValues;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class GetDeclarationFilterDataResult implements Result {

	private List<Department> departments;
	private List<TaxPeriod> taxPeriods;
	private List<DeclarationType> declarationTypes;
	private FormDataFilterAvailableValues filterValues;

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

	public List<DeclarationType> getDeclarationTypes() {
		return declarationTypes;
	}

	public void setDeclarationTypes(List<DeclarationType> declarationTypes) {
		this.declarationTypes = declarationTypes;
	}

	public FormDataFilterAvailableValues getFilterValues() {
		return filterValues;
	}

	public void setFilterValues(FormDataFilterAvailableValues filterValues) {
		this.filterValues = filterValues;
	}
}
