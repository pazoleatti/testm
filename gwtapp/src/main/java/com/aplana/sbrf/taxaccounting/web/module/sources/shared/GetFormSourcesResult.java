package com.aplana.sbrf.taxaccounting.web.module.sources.shared;

import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;
import java.util.Map;

public class GetFormSourcesResult implements Result {
	private static final long serialVersionUID = -2062704305582222051L;
	
	private Map<Integer, FormType> formTypes;
	private List<DepartmentFormType> formSources;

	public List<DepartmentFormType> getFormSources() {
		return formSources;
	}

	public void setFormSources(List<DepartmentFormType> formSources) {
		this.formSources = formSources;
	}

	public Map<Integer, FormType> getFormTypes() {
		return formTypes;
	}

	public void setFormTypes(Map<Integer, FormType> formTypes) {
		this.formTypes = formTypes;
	}
}
