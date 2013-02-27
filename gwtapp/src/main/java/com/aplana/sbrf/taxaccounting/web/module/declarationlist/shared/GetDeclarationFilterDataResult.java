package com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared;


import com.aplana.sbrf.taxaccounting.model.*;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class GetDeclarationFilterDataResult implements Result {

	private ReportPeriod currentReportPeriod;
	private List<Department> departments;
	private List<TaxPeriod> taxPeriods;
	private DeclarationDataFilterAvailableValues filterValues;

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

	public DeclarationDataFilterAvailableValues getFilterValues() {
		return filterValues;
	}

	public void setFilterValues(DeclarationDataFilterAvailableValues filterValues) {
		this.filterValues = filterValues;
	}

	public ReportPeriod getCurrentReportPeriod() {
		return currentReportPeriod;
	}

	public void setCurrentReportPeriod(ReportPeriod currentReportPeriod) {
		this.currentReportPeriod = currentReportPeriod;
	}
}
