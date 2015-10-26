package com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared;


import com.aplana.sbrf.taxaccounting.model.*;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class GetDeclarationFilterDataResult implements Result {
	private static final long serialVersionUID = 1304339398716140386L;
	
	private List<Department> departments;
	private List<ReportPeriod> periods;
	private DeclarationDataFilterAvailableValues filterValues;
    private DeclarationDataFilter defaultDecFilterData;
    private Integer userDepartmentId;

	public List<Department> getDepartments() {
		return departments;
	}

	public void setDepartments(List<Department> departments) {
		this.departments = departments;
	}

	public DeclarationDataFilterAvailableValues getFilterValues() {
		return filterValues;
	}

	public void setFilterValues(DeclarationDataFilterAvailableValues filterValues) {
		this.filterValues = filterValues;
	}

	public List<ReportPeriod> getPeriods() {
		return periods;
	}

	public void setPeriods(List<ReportPeriod> periods) {
		this.periods = periods;
	}

    public DeclarationDataFilter getDefaultDecFilterData() {
        return defaultDecFilterData;
    }

    public void setDefaultDecFilterData(DeclarationDataFilter defaultDecFilterData) {
        this.defaultDecFilterData = defaultDecFilterData;
    }

    public Integer getUserDepartmentId() {
        return userDepartmentId;
    }

    public void setUserDepartmentId(Integer userDepartmentId) {
        this.userDepartmentId = userDepartmentId;
    }
}
