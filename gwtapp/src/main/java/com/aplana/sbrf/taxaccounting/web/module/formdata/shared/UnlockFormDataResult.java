package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.gwtplatform.dispatch.shared.Result;

public class UnlockFormDataResult implements Result {
	private static final long serialVersionUID = -7720321592508896091L;
	
	private boolean unlockedSuccessfully;

	public boolean isUnlockedSuccessfully() {
		return unlockedSuccessfully;
	}

	public void setUnlockedSuccessfully(boolean unlockedSuccessfully) {
		this.unlockedSuccessfully = unlockedSuccessfully;
	}
}
