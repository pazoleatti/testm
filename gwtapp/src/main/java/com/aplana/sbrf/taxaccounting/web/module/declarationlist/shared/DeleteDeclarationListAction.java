package com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

public class DeleteDeclarationListAction extends UnsecuredActionImpl<DeleteDeclarationListResult> implements ActionName {
	private List<Long> declarationIds;

    public List<Long> getDeclarationIds() {
        return declarationIds;
    }

    public void setDeclarationIds(List<Long> declarationIds) {
        this.declarationIds = declarationIds;
    }

    @Override
    public String getName() {
        return "Удаление налоговых форм";
    }
}
