package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class UpdateDeclarationDataAction extends UnsecuredActionImpl<UpdateDeclarationDataResult> {
    private DeclarationData declarationData;
	private boolean isRefresh;
	private boolean isDelete;

    public DeclarationData getDeclarationData() {
        return declarationData;
    }

    public void setDeclarationData(DeclarationData declaration) {
        this.declarationData = declaration;
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
