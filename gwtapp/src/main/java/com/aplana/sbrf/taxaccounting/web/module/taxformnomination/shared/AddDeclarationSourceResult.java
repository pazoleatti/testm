package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared;

import com.gwtplatform.dispatch.shared.Result;

public class AddDeclarationSourceResult implements Result {
	private static final long serialVersionUID = 8073125605977210012L;

	private String uuid;
    private boolean issetRelations;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

    public boolean isIssetRelations() {
        return issetRelations;
    }

    public void setIssetRelations(boolean issetRelations) {
        this.issetRelations = issetRelations;
    }
}
