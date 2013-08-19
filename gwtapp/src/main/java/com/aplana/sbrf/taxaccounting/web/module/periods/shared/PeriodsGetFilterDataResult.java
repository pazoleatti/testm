package com.aplana.sbrf.taxaccounting.web.module.periods.shared;

import com.aplana.sbrf.taxaccounting.model.*;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class PeriodsGetFilterDataResult implements Result {

	private ReportPeriod currentReportPeriod;
    private List<Department> departments;
	private FormDataFilterAvailableValues filterValues;
	private List<DictionaryTaxPeriod> dictionaryTaxPeriods;
	private int yearFrom;
	private int yearTo;
	private int currentYear;
	private Department selectedDepartment;
	private boolean enableDepartmentPicker;
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

	public int getYearFrom() {
		return yearFrom;
	}

	public void setYearFrom(int yearFrom) {
		this.yearFrom = yearFrom;
	}

	public int getYearTo() {
		return yearTo;
	}

	public void setYearTo(int yearTo) {
		this.yearTo = yearTo;
	}

	public int getCurrentYear() {
		return currentYear;
	}

	public void setCurrentYear(int currentYear) {
		this.currentYear = currentYear;
	}

	public Department getSelectedDepartment() {
		return selectedDepartment;
	}

	public void setSelectedDepartment(Department selectedDepartment) {
		this.selectedDepartment = selectedDepartment;
	}

	public boolean isEnableDepartmentPicker() {
		return enableDepartmentPicker;
	}

	public void setEnableDepartmentPicker(boolean enableDepartmentPicker) {
		this.enableDepartmentPicker = enableDepartmentPicker;
	}
}
