package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class UpdateDeclarationAction extends UnsecuredActionImpl<UpdateDeclarationResult> {
    private DeclarationData declaration;
	private boolean isRefresh;
	private boolean isDelete;

    public DeclarationData getDeclaration() {
        return declaration;
    }

    public void setDeclaration(DeclarationData declaration) {
        this.declaration = declaration;
    }

	public boolean isRefresh() {
		return isRefresh;
	}

	public void setRefresh(boolean refresh) {
		isRefresh = refresh;
	}

	public boolean isDelete() {
		return isDelete;
	}

	public void setDelete(boolean delete) {
		isDelete = delete;
	}
}
