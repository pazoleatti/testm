package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class UnlockFormData extends UnsecuredActionImpl<UnlockFormDataResult> implements ActionName {

	private long formId;
    private boolean manual;
    private boolean isPerformerLock;
    private boolean readOnlyMode;
    private String msg;

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

    public boolean isReadOnlyMode() {
        return readOnlyMode;
    }

    public void setReadOnlyMode(boolean readOnlyMode) {
        this.readOnlyMode = readOnlyMode;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
	public String getName() {
		return "Разблокировка формы";
	}
}
