package com.aplana.sbrf.taxaccounting.web.module.periods.shared;

import com.aplana.sbrf.taxaccounting.model.*;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class PeriodsGetFilterDataResult implements Result {

	private ReportPeriod currentReportPeriod;
    private List<Department> departments;
	private FormDataFilterAvailableValues filterValues;
	private List<DictionaryTaxPeriod> dictionaryTaxPeriods;
//	private List<>

    public PeriodsGetFilterDataResult(){

    }

    public List<Department> getDepartments() {
        return departments;
    }

    public void setDepartments(List<Department> departments) {
        this.departments = departments;
    }

	public FormDataFilterAvailableValues getFilterValues() {
		return filterValues;
	}

	public void setFilterValues(FormDataFilterAvailableValues filterValues) {
		this.filterValues = filterValues;
	}

	public ReportPeriod getCurrentReportPeriod() {
		return currentReportPeriod;
	}

	public void setCurrentReportPeriod(ReportPeriod currentReportPeriod) {
		this.currentReportPeriod = currentReportPeriod;
	}

	public List<DictionaryTaxPeriod> getDictionaryTaxPeriods() {
		return dictionaryTaxPeriods;
	}

	public void setDictionaryTaxPeriods(List<DictionaryTaxPeriod> dictionaryTaxPeriods) {
		this.dictionaryTaxPeriods = dictionaryTaxPeriods;
	}
}
