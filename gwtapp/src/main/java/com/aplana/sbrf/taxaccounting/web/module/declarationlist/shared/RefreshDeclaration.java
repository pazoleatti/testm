package com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class RefreshDeclaration extends UnsecuredActionImpl<RefreshDeclarationResult> implements ActionName {

	private long declarationDataId;
    private Integer pagesCount;

	public long getDeclarationDataId() {
		return declarationDataId;
	}

	public void setDeclarationDataId(long declarationDataId) {
		this.declarationDataId = declarationDataId;
	}

    public Integer getPagesCount() {
        return pagesCount;
    }

    public void setPagesCount(Integer pagesCount) {
        this.pagesCount = pagesCount;
    }

    @Override
	public String getName() {
		return "Переформирования декларации";
	}
}
