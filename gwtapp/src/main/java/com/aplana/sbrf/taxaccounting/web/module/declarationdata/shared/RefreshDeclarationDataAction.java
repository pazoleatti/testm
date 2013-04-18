package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class RefreshDeclarationDataAction extends UnsecuredActionImpl<RefreshDeclarationDataResult> {
	private long declarationId;
	private String docDate;

	public long getDeclarationId() {
		return declarationId;
	}

	public void setDeclarationId(long declarationId) {
		this.declarationId = declarationId;
	}

	public String getDocDate() {
		return docDate;
	}

	public void setDocDate(String docDate) {
		this.docDate = docDate;
	}
}
