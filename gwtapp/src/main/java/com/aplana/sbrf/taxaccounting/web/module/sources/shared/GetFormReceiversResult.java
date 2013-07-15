package com.aplana.sbrf.taxaccounting.web.module.sources.shared;

import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;
import java.util.Map;

public class GetFormReceiversResult implements Result {
	private Map<Integer, FormType> formTypes;
	private List<DepartmentFormType> formReceivers;

	public List<DepartmentFormType> getFormReceivers() {
		return formReceivers;
	}

	public void setFormReceivers(List<DepartmentFormType> formReceivers) {
		this.formReceivers = formReceivers;
	}

	public Map<Integer, FormType> getFormTypes() {
		return formTypes;
	}

	public void setFormTypes(Map<Integer, FormType> formTypes) {
		this.formTypes = formTypes;
	}
}
