package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class DeleteDeclarationDataAction extends UnsecuredActionImpl<DeleteDeclarationDataResult> implements ActionName {
	private long declarationId;

	public long getDeclarationId() {
		return declarationId;
	}

	public void setDeclarationId(long declarationId) {
		this.declarationId = declarationId;
	}

    @Override
    public String getName() {
        return "Удалить";
    }
}
