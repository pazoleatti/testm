package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.aplana.sbrf.taxaccounting.model.Declaration;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class UpdateDeclarationAction extends UnsecuredActionImpl<UpdateDeclarationResult> {
    private Declaration declaration;
	private boolean isRefresh;

    public Declaration getDeclaration() {
        return declaration;
    }

    public void setDeclaration(Declaration declaration) {
        this.declaration = declaration;
    }

	public boolean isRefresh() {
		return isRefresh;
	}

	public void setRefresh(boolean refresh) {
		isRefresh = refresh;
	}
}
