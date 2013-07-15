package com.aplana.sbrf.taxaccounting.web.module.sources.shared;

import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;
import java.util.Map;

public class GetDeclarationReceiverSourcesResult implements Result {
	private Map<Integer, FormType> formTypes;
	private List<DepartmentFormType> formReceiverSources;

	public List<DepartmentFormType> getFormReceiverSources() {
		return formReceiverSources;
	}

	public void setFormReceiverSources(List<DepartmentFormType> formReceiverSources) {
		this.formReceiverSources = formReceiverSources;
	}

	public Map<Integer, FormType> getFormTypes() {
		return formTypes;
	}

	public void setFormTypes(Map<Integer, FormType> formTypes) {
		this.formTypes = formTypes;
	}
}
