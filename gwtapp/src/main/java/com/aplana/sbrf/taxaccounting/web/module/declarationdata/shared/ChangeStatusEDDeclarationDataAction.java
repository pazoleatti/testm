package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class ChangeStatusEDDeclarationDataAction extends UnsecuredActionImpl<ChangeStatusEDDeclarationDataResult> implements ActionName {
	private long declarationId;
    private Long docStateId;

	public long getDeclarationId() {
		return declarationId;
	}

	public void setDeclarationId(long declarationId) {
		this.declarationId = declarationId;
	}

    public Long getDocStateId() {
        return docStateId;
    }

    public void setDocStateId(Long docStateId) {
        this.docStateId = docStateId;
    }

    @Override
    public String getName() {
        return "Изменить состояние ЭД";
    }
}
