package com.aplana.sbrf.taxaccounting.web.module.members.shared;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class FilterValues implements Result {
	List<TARole> roles;
	List<Department> departments;

	public List<TARole> getRoles() {
		return roles;
	}

	public void setRoles(List<TARole> roles) {
		this.roles = roles;
	}

	public List<Department> getDepartments() {
		return departments;
	}

	public void setDepartments(List<Department> departments) {
		this.departments = departments;
	}
}
