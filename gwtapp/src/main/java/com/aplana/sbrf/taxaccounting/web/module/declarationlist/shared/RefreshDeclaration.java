package com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class RefreshDeclaration extends UnsecuredActionImpl<RefreshDeclarationResult> implements ActionName {

	long declarationDataId;

	public long getDeclarationDataId() {
		return declarationDataId;
	}

	public void setDeclarationDataId(long declarationDataId) {
		this.declarationDataId = declarationDataId;
	}

	@Override
	public String getName() {
		return "Переформирования декларации";
	}
}
