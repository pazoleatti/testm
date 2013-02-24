package com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared;

import com.aplana.sbrf.taxaccounting.model.*;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class GetFilterDataResult implements Result {

    private List<Department> departments;
	private List<TaxPeriod> taxPeriods;
	private FormDataFilterAvailableValues filterValues;

    public GetFilterDataResult(){

    }

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

	public FormDataFilterAvailableValues getFilterValues() {
		return filterValues;
	}

	public void setFilterValues(FormDataFilterAvailableValues filterValues) {
		this.filterValues = filterValues;
	}

}
