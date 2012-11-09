package com.aplana.sbrf.taxaccounting.gwtapp.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class GetFormData extends UnsecuredActionImpl<GetFormDataResult> {
	private long formDataId;

	public long getFormDataId() {
		return formDataId;
	}

	public void setFormDataId(long formDataId) {
		this.formDataId = formDataId;
	}
}
