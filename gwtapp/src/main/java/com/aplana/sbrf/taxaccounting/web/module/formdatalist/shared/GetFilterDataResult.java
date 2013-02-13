package com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class GetFilterDataResult implements Result {

    private List<Department> departments;
    private List<FormType> types;
	private List<TaxPeriod> taxPeriods;

    public GetFilterDataResult(){

    }

    public List<Department> getDepartments() {
        return departments;
    }

    public void setDepartments(List<Department> departments) {
        this.departments = departments;
    }

    public List<FormType> getFormTypes() {
        return types;
    }

    public void setFormTypes(List<FormType> kinds) {
        this.types = kinds;
    }

	public List<TaxPeriod> getTaxPeriods() {
		return taxPeriods;
	}

	public void setTaxPeriods(List<TaxPeriod> taxPeriods) {
		this.taxPeriods = taxPeriods;
	}
}
