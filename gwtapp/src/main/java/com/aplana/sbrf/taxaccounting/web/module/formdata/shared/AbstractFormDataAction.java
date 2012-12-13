package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * @author Vitalii Samolovskikh
 */
public class AbstractFormDataAction extends UnsecuredActionImpl<FormDataResult> {
	private FormData formData;

	public FormData getFormData() {
		return formData;
	}

	public void setFormData(FormData formData) {
		this.formData = formData;
	}
}
