package com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared;

import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class GetDeclarationTypeResult implements Result {
	private static final long serialVersionUID = 6048433881484626479L;

	List<DeclarationType> declarationTypes;

	public List<DeclarationType> getDeclarationTypes() {
		return declarationTypes;
	}

	public void setDeclarationTypes(List<DeclarationType> declarationTypes) {
		this.declarationTypes = declarationTypes;
	}
}
