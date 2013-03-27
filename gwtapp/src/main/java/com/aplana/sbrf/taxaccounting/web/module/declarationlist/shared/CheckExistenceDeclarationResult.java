package com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared;

import com.gwtplatform.dispatch.shared.Result;

public class CheckExistenceDeclarationResult implements Result {

	boolean exist;

	Long declarationDataId;

	public Long getDeclarationDataId() {
		return declarationDataId;
	}

	public void setDeclarationDataId(Long declarationDataId) {
		this.declarationDataId = declarationDataId;
	}

	public boolean isExist() {
		return exist;
	}

	public void setExist(boolean exist) {
		this.exist = exist;
	}
}
