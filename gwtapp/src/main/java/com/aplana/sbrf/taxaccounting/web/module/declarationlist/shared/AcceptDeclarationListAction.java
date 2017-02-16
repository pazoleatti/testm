package com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

public class AcceptDeclarationListAction extends UnsecuredActionImpl<AcceptDeclarationListResult> implements ActionName {
    private List<Long> declarationIds;
	private boolean accepted;
	private String reasonForReturn;
    private TaxType taxType;

    public List<Long> getDeclarationIds() {
        return declarationIds;
    }

    public void setDeclarationIds(List<Long> declarationIds) {
        this.declarationIds = declarationIds;
    }

    public boolean isAccepted() {
		return accepted;
	}

	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
	}

	public String getReasonForReturn() {
		return reasonForReturn;
	}

	public void setReasonForReturn(String reasonForReturn) {
		this.reasonForReturn = reasonForReturn;
	}

    public TaxType getTaxType() {
        return taxType;
    }

    public void setTaxType(TaxType taxType) {
        this.taxType = taxType;
    }

    @Override
    public String getName() {
        return (accepted ? "Принять" : "Отменить принятие");
    }
}
