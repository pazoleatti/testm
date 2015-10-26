package com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared;

import com.aplana.sbrf.taxaccounting.model.*;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class GetFilterDataResult implements Result {
	private static final long serialVersionUID = -3436595935051736909L;

    private List<Department> departments;
	private List<ReportPeriod> reportPeriods;
	private FormDataFilterAvailableValues filterValues;
	private FormDataFilter defaultFilter;
    private Integer userDepartmentId;

    public GetFilterDataResult(){

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

	public List<ReportPeriod> getReportPeriods() {
		return reportPeriods;
	}

	public void setReportPeriods(List<ReportPeriod> reportPeriods) {
		this.reportPeriods = reportPeriods;
	}

	public FormDataFilter getDefaultFilter() {
		return defaultFilter;
	}

	public void setDefaultFilter(FormDataFilter defaultFilter) {
		this.defaultFilter = defaultFilter;
	}

    public Integer getUserDepartmentId() {
        return userDepartmentId;
    }

    public void setUserDepartmentId(Integer userDepartmentId) {
        this.userDepartmentId = userDepartmentId;
    }
}
