package com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

public class ChangeStatusEDDeclarationListAction extends UnsecuredActionImpl<ChangeStatusEDDeclarationListResult> implements ActionName {
	private List<Long> declarationIds;
    private Long docStateId;

    public List<Long> getDeclarationIds() {
        return declarationIds;
    }

    public void setDeclarationIds(List<Long> declarationIds) {
        this.declarationIds = declarationIds;
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
