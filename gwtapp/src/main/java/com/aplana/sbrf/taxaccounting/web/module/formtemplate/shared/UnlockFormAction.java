package com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared;


import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class UnlockFormAction extends UnsecuredActionImpl<UnlockFormResult> {

	private int formId;

	public int getFormId() {
		return formId;
	}

	public void setFormId(int formId) {
		this.formId = formId;
	}
}
