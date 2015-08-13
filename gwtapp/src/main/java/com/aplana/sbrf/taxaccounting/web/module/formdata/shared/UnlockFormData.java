package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class UnlockFormData extends UnsecuredActionImpl<UnlockFormDataResult> implements ActionName {

	private long formId;
    private boolean manual;
    private boolean isPerformerLock;

	public long getFormId() {
		return formId;
	}

	public void setFormId(long formId) {
		this.formId = formId;
	}

    public boolean isManual() {
        return manual;
    }

    public void setManual(boolean manual) {
        this.manual = manual;
    }

    public boolean isPerformerLock() {
        return isPerformerLock;
    }

    public void setPerformerLock(boolean isPerformerLock) {
        this.isPerformerLock = isPerformerLock;
    }

    @Override
	public String getName() {
		return "Разблокировка формы";
	}
}
