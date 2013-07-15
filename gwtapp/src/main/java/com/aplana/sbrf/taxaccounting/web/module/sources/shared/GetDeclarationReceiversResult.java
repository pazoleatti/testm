package com.aplana.sbrf.taxaccounting.web.module.sources.shared;

import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.DepartmentDeclarationType;
import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;
import java.util.Map;

public class GetDeclarationReceiversResult implements Result {
	private Map<Integer, DeclarationType> declarationTypes;
	private List<DepartmentDeclarationType> declarationReceivers;

	public Map<Integer, DeclarationType> getDeclarationTypes() {
		return declarationTypes;
	}

	public void setDeclarationTypes(Map<Integer, DeclarationType> declarationTypes) {
		this.declarationTypes = declarationTypes;
	}

	public List<DepartmentDeclarationType> getDeclarationReceivers() {
		return declarationReceivers;
	}

	public void setDeclarationReceivers(List<DepartmentDeclarationType> declarationReceivers) {
		this.declarationReceivers = declarationReceivers;
	}
}
