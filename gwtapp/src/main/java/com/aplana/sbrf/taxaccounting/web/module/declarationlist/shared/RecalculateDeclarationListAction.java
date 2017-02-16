package com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.Date;
import java.util.List;

public class RecalculateDeclarationListAction extends UnsecuredActionImpl<RecalculateDeclarationListResult> implements ActionName {
    private List<Long> declarationIds;
	private Date docDate;
    private TaxType taxType;

    public List<Long> getDeclarationIds() {
        return declarationIds;
    }

    public void setDeclarationIds(List<Long> declarationIds) {
        this.declarationIds = declarationIds;
    }

    public Date getDocDate() {
		return docDate;
	}

	public void setDocDate(Date docDate) {
		this.docDate = docDate;
	}

    public TaxType getTaxType() {
        return taxType;
    }

    public void setTaxType(TaxType taxType) {
        this.taxType = taxType;
    }

    @Override
    public String getName() {
        return "Рассчитать";
    }
}
