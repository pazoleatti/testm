package com.aplana.sbrf.taxaccounting.web.module.periods.shared;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentPair;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class GetDeadlineDepartmentsResult implements Result {
	private List<Department> departments;
	private DepartmentPair selectedDepartment;

	public List<Department> getDepartments() {
		return departments;
	}

	public void setDepartments(List<Department> departments) {
		this.departments = departments;
	}

	public DepartmentPair getSelectedDepartment() {
		return selectedDepartment;
	}

	public void setSelectedDepartment(DepartmentPair selectedDepartment) {
		this.selectedDepartment = selectedDepartment;
	}
}
