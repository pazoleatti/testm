package com.aplana.sbrf.taxaccounting.web.module.sources.shared;

import java.util.List;
import java.util.Map;

import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.DepartmentDeclarationType;
import com.gwtplatform.dispatch.shared.Result;

public class GetDeclarationReceiversResult implements Result {
	private static final long serialVersionUID = 3110701297898380844L;
	
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
