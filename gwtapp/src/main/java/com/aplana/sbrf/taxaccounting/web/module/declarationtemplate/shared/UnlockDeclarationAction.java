package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class UnlockDeclarationAction extends UnsecuredActionImpl<UnlockDeclarationResult> {

	private int declarationId;

	public int getDeclarationId() {
		return declarationId;
	}

	public void setDeclarationId(int declarationId) {
		this.declarationId = declarationId;
	}
}
