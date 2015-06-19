package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class AcceptDeclarationDataAction extends UnsecuredActionImpl<AcceptDeclarationDataResult> implements ActionName {
    private long declarationId;
	private boolean accepted;
	private String reasonForReturn;
    private TaxType taxType;
    private boolean force;
    private boolean cancelTask;

    public long getDeclarationId() {
		return declarationId;
	}

	public void setDeclarationId(long declarationId) {
		this.declarationId = declarationId;
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

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public boolean isCancelTask() {
        return cancelTask;
    }

    public void setCancelTask(boolean cancelTask) {
        this.cancelTask = cancelTask;
    }

    @Override
    public String getName() {
        return (accepted ? "Принять" : "Отменить принятие");
    }
}
