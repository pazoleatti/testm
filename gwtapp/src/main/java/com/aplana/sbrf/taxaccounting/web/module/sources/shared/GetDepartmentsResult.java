package com.aplana.sbrf.taxaccounting.web.module.sources.shared;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;
import java.util.Set;

public class GetDepartmentsResult implements Result {
	private static final long serialVersionUID = 6889603073251885676L;
	
	private Set<Integer> availableDepartments;
	
	private List<Department> departments;

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


}
