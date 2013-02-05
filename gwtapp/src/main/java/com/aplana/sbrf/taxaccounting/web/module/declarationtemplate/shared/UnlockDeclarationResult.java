package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared;

import com.gwtplatform.dispatch.shared.Result;

public class UnlockDeclarationResult implements Result {

	private boolean unlockedSuccessfully;

	public boolean isUnlockedSuccessfully() {
		return unlockedSuccessfully;
	}

	public void setUnlockedSuccessfully(boolean unlockedSuccessfully) {
		this.unlockedSuccessfully = unlockedSuccessfully;
	}
}

