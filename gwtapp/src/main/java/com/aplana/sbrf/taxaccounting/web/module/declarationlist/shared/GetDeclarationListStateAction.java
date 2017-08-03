package com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

public class GetDeclarationListStateAction extends UnsecuredActionImpl<GetDeclarationListStateResult> implements ActionName {
	private List<Long> declarationIds;
    private TaxType taxType;

    public List<Long> getDeclarationIds() {
        return declarationIds;
    }

    public void setDeclarationIds(List<Long> declarationIds) {
        this.declarationIds = declarationIds;
    }

    public TaxType getTaxType() {
        return taxType;
    }

    public void setTaxType(TaxType taxType) {
        this.taxType = taxType;
    }

    @Override
    public String getName() {
        return "Получение статусов налоговых форм";
    }
}
