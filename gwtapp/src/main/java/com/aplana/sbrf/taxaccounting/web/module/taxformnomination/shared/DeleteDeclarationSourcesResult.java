package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared;

import com.gwtplatform.dispatch.shared.Result;

public class DeleteDeclarationSourcesResult implements Result {
	private static final long serialVersionUID = 1837776652451421385L;

	private String uuid;
    private boolean existDeclaration;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

    public boolean isExistDeclaration() {
        return existDeclaration;
    }

    public void setExistDeclaration(boolean existDeclaration) {
        this.existDeclaration = existDeclaration;
    }
}
