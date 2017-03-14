package com.aplana.sbrf.taxaccounting.web.module.members.shared;

import com.gwtplatform.dispatch.shared.Result;

public class SaveUserResult implements Result {
	private static final long serialVersionUID = 1561651849841533047L;

	private String uuid;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
}
