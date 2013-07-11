package com.aplana.sbrf.taxaccounting.web.module.sources.shared;

import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;
import java.util.Map;

public class GetFormReceiversResult implements Result {
	private Map<Integer, String> formTypeNames;
	private List<DepartmentFormType> receiversDepartmentFormTypes;

	public List<DepartmentFormType> getReceiversDepartmentFormTypes() {
		return receiversDepartmentFormTypes;
	}

	public void setReceiversDepartmentFormTypes(List<DepartmentFormType> receiversDepartmentFormTypes) {
		this.receiversDepartmentFormTypes = receiversDepartmentFormTypes;
	}

	public Map<Integer, String> getFormTypeNames() {
		return formTypeNames;
	}

	public void setFormTypeNames(Map<Integer, String> formTypeNames) {
		this.formTypeNames = formTypeNames;
	}
}
