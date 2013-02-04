package com.aplana.sbrf.taxaccounting.web.module.admin.shared;

import com.gwtplatform.dispatch.shared.Result;

public class UnlockFormResult implements Result {

	private boolean unlockedSuccessfully;

	public boolean isUnlockedSuccessfully() {
		return unlockedSuccessfully;
	}

	public void setUnlockedSuccessfully(boolean unlockedSuccessfully) {
		this.unlockedSuccessfully = unlockedSuccessfully;
	}
}

