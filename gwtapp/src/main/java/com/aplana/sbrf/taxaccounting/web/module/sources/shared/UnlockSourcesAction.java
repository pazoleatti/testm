package com.aplana.sbrf.taxaccounting.web.module.sources.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class UnlockSourcesAction extends UnsecuredActionImpl<UnlockSourcesResult> {

	private int declarationId;

	public int getDeclarationId() {
		return declarationId;
	}

	public void setDeclarationId(int declarationId) {
		this.declarationId = declarationId;
	}
}
