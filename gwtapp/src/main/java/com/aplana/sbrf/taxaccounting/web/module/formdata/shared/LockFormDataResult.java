package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.gwtplatform.dispatch.shared.Result;

public class LockFormDataResult implements Result {

	private boolean lockedSuccessfully;

	public boolean isLockedSuccessfully() {
		return lockedSuccessfully;
	}

	public void setLockedSuccessfully(boolean lockedSuccessfully) {
		this.lockedSuccessfully = lockedSuccessfully;
	}
}
