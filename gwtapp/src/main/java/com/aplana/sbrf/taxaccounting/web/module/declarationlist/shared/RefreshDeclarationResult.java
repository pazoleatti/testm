package com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared;

import com.gwtplatform.dispatch.shared.Result;

public class RefreshDeclarationResult implements Result {
	private static final long serialVersionUID = 6387854809139910782L;
	
	boolean success;

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}
}
