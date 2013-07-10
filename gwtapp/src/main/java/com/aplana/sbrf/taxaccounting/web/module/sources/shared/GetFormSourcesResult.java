package com.aplana.sbrf.taxaccounting.web.module.sources.shared;

import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;
import java.util.Map;

public class GetFormSourcesResult implements Result {
	private Map<Integer, String> formTypeNames;
	private List<DepartmentFormType> sourcesDepartmentFormTypes;

	public List<DepartmentFormType> getSourcesDepartmentFormTypes() {
		return sourcesDepartmentFormTypes;
	}

	public void setSourcesDepartmentFormTypes(List<DepartmentFormType> sourcesDepartmentFormTypes) {
		this.sourcesDepartmentFormTypes = sourcesDepartmentFormTypes;
	}

	public Map<Integer, String> getFormTypeNames() {
		return formTypeNames;
	}

	public void setFormTypeNames(Map<Integer, String> formTypeNames) {
		this.formTypeNames = formTypeNames;
	}
}
