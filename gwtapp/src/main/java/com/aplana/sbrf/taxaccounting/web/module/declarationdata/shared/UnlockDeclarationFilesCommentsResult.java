package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

public class UnlockDeclarationFilesCommentsResult extends DeclarationDataResult {
	private static final long serialVersionUID = -1465465465184841327L;
	
	private boolean unlockedSuccessfully;

	public boolean isUnlockedSuccessfully() {
		return unlockedSuccessfully;
	}

	public void setUnlockedSuccessfully(boolean unlockedSuccessfully) {
		this.unlockedSuccessfully = unlockedSuccessfully;
	}
}
