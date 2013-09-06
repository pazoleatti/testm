package com.aplana.sbrf.taxaccounting.web.module.sources.shared;

import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;
import java.util.Map;

public class GetFormDFTResult implements Result {
	private static final long serialVersionUID = -5102125212590725720L;
	
	private Map<Integer, FormType> formTypes;
	private List<DepartmentFormType> departmentFormTypes;


	public Map<Integer, FormType> getFormTypes() {
		return formTypes;
	}

	public void setFormTypes(Map<Integer, FormType> formTypes) {
		this.formTypes = formTypes;
	}

	public List<DepartmentFormType> getDepartmentFormTypes() {
		return departmentFormTypes;
	}

	public void setDepartmentFormTypes(List<DepartmentFormType> departmentFormTypes) {
		this.departmentFormTypes = departmentFormTypes;
	}
}
