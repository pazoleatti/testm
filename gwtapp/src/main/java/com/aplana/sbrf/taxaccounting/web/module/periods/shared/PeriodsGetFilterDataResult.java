package com.aplana.sbrf.taxaccounting.web.module.periods.shared;

import com.aplana.sbrf.taxaccounting.model.*;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;
import java.util.Set;

public class PeriodsGetFilterDataResult implements Result {
	private static final long serialVersionUID = 3768478656977415062L;

	private ReportPeriod currentReportPeriod;
   
	private int yearFrom;
	private int yearTo;
	private int currentYear;
	private TaxType taxType;
	
	private List<Department> departments;
	private Set<Integer> avalDepartments;
	private DepartmentPair selectedDepartment;
	private boolean enableDepartmentPicker;
	private boolean readOnly;

    public PeriodsGetFilterDataResult(){

    }

    public List<Department> getDepartments() {
        return departments;
    }

    public void setDepartments(List<Department> departments) {
        this.departments = departments;
    }

	public ReportPeriod getCurrentReportPeriod() {
		return currentReportPeriod;
	}

	public void setCurrentReportPeriod(ReportPeriod currentReportPeriod) {
		this.currentReportPeriod = currentReportPeriod;
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


	public boolean isEnableDepartmentPicker() {
		return enableDepartmentPicker;
	}

	public void setEnableDepartmentPicker(boolean enableDepartmentPicker) {
		this.enableDepartmentPicker = enableDepartmentPicker;
	}

	public Set<Integer> getAvalDepartments() {
		return avalDepartments;
	}

	public void setAvalDepartments(Set<Integer> avalDepartments) {
		this.avalDepartments = avalDepartments;
	}

	public DepartmentPair getSelectedDepartment() {
		return selectedDepartment;
	}

	public void setSelectedDepartment(DepartmentPair selectedDepartment) {
		this.selectedDepartment = selectedDepartment;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public TaxType getTaxType() {
		return taxType;
	}

	public void setTaxType(TaxType taxType) {
		this.taxType = taxType;
	}
}
