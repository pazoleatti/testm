package com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.gwtplatform.dispatch.shared.Result;

public class GetFormDataResult implements Result {
	private FormData formData;
	
	public FormData getFormData() {
		return formData;
	}
	
	public void setFormData(FormData formData) {
		this.formData = formData;
	}
}