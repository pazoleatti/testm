package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared;

import com.gwtplatform.dispatch.shared.Result;

public class DeleteDeclarationSourcesResult implements Result {
	private static final long serialVersionUID = 1837776652451421385L;

	private String uuid;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
}
