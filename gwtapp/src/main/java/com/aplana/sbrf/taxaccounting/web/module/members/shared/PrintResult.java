package com.aplana.sbrf.taxaccounting.web.module.members.shared;

import com.gwtplatform.dispatch.shared.Result;

public class PrintResult  implements Result {
	private static final long serialVersionUID = 4937617163429596768L;

	private String uuid;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
}
