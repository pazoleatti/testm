package com.aplana.sbrf.taxaccounting.controller.formdata;

import com.aplana.sbrf.taxaccounting.model.Form;
import com.aplana.sbrf.taxaccounting.model.FormData;

/**
 * Бин для хранения информации о редактируемых данных налоговой формы
 */
public class EditFormDataBean {
	private FormData formData;

	public FormData getFormData() {
		return formData;
	}

	public void setFormData(FormData formData) {
		this.formData = formData;
	}
	
	public Form getForm() {
		return formData.getForm();
	}
}
