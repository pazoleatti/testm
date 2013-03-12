package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class AcceptDeclarationDataAction extends UnsecuredActionImpl<AcceptDeclarationDataResult> {
    private long declarationId;
	private boolean accepted;

	public long getDeclarationId() {
		return declarationId;
	}

	public void setDeclarationId(long declarationId) {
		this.declarationId = declarationId;
	}

	public boolean isAccepted() {
		return accepted;
	}

	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
	}
}
