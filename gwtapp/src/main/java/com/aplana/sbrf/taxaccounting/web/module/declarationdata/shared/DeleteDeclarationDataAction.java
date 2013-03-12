package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class DeleteDeclarationDataAction extends UnsecuredActionImpl<DeleteDeclarationDataResult> {
	private long declarationId;

	public long getDeclarationId() {
		return declarationId;
	}

	public void setDeclarationId(long declarationId) {
		this.declarationId = declarationId;
	}
}
