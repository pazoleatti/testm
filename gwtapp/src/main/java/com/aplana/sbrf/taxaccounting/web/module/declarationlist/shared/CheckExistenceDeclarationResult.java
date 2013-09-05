package com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared;

import com.gwtplatform.dispatch.shared.Result;

public class CheckExistenceDeclarationResult implements Result {
	private static final long serialVersionUID = 3267048502835351877L;

	public static enum DeclarationStatus {
		EXIST_ACCEPTED, // Существует и в статусе принята
		EXIST_CREATED, // Существует и в статусе создана
		NOT_EXIST// Не существует
	}
	DeclarationStatus status;

	Long declarationDataId;

	public Long getDeclarationDataId() {
		return declarationDataId;
	}

	public void setDeclarationDataId(Long declarationDataId) {
		this.declarationDataId = declarationDataId;
	}

	public DeclarationStatus getStatus() {
		return status;
	}

	public void setStatus(DeclarationStatus status) {
		this.status = status;
	}
}
