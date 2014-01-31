package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared;

import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;
import java.util.Set;

public class GetDeclarationPopUpFilterResult implements Result {
	private static final long serialVersionUID = -6772791747226893212L;

	Set<Integer> availableDepartmentSet;
	List<Department> departments;
	List<DeclarationType> declarationTypes;

	public List<Department> getDepartments() {
		return departments;
	}

	public void setDepartments(List<Department> departments) {
		this.departments = departments;
	}

	public Set<Integer> getAvailableDepartments() {
		return availableDepartmentSet;
	}

	public void setAvailableDepartments(Set<Integer> availableDepartmentSet) {
		this.availableDepartmentSet = availableDepartmentSet;
	}

	public List<DeclarationType> getDeclarationTypes() {
		return declarationTypes;
	}

	public void setDeclarationTypes(List<DeclarationType> declarationTypes) {
		this.declarationTypes = declarationTypes;
	}
}
