package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.gwtplatform.dispatch.shared.Result;

public class GetFormDataResult implements Result {
	private FormData formData;
	
	private AccessFlags accessFlags;
	
	public FormData getFormData() {
		return formData;
	}
	
	public void setFormData(FormData formData) {
		this.formData = formData;
	}

	public AccessFlags getAccessFlags() {
		return accessFlags;
	}

	public void setAccessFlags(AccessFlags accessFlags) {
		this.accessFlags = accessFlags;
	}
}