package com.aplana.sbrf.taxaccounting.web.module.sources.shared;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.PeriodInfo;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;
import java.util.Set;

public class InitSourcesResult implements Result {
	private static final long serialVersionUID = 6889603073251885676L;
	
	private List<Department> departments;
    private Set<Integer> availableDepartments;
    private Integer defaultDepartment;

    private int year;
    private List<PeriodInfo> periods;

    private boolean isControlUNP;

    public List<PeriodInfo> getPeriods() {
        return periods;
    }

    public void setPeriods(List<PeriodInfo> periods) {
        this.periods = periods;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public List<Department> getDepartments() {
		return departments;
	}

	public void setDepartments(List<Department> departments) {
		this.departments = departments;
	}

	public Set<Integer> getAvailableDepartments() {
		return availableDepartments;
	}

	public void setAvailableDepartments(Set<Integer> availableDepartments) {
		this.availableDepartments = availableDepartments;
	}

    public Integer getDefaultDepartment() {
        return defaultDepartment;
    }

    public void setDefaultDepartment(Integer defaultDepartment) {
        this.defaultDepartment = defaultDepartment;
    }

    public boolean isControlUNP() {
        return isControlUNP;
    }

    public void setControlUNP(boolean isControlUNP) {
        this.isControlUNP = isControlUNP;
    }
}
