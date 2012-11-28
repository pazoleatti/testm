package com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class GetFilterDataResult implements Result {

    public GetFilterDataResult(){

    }

    private List<Department> departments;
    private List<FormType> types;
	private List<ReportPeriod> periods;

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

	public List<ReportPeriod> getPeriods() {
		return periods;
	}

	public void setPeriods(List<ReportPeriod> periods) {
		this.periods = periods;
	}

}
