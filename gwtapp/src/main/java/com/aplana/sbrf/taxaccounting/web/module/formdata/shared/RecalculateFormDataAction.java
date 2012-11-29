package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;
/**
 * 
 * @author Eugene Stetsenko
 * Запрос для пересчета формы
 *
 */
public class RecalculateFormDataAction extends UnsecuredActionImpl<RecalculateFormDataResult> {
	private FormData formData;
	
	public FormData getFormData() {
		return formData;
	}
	public void setFormData(FormData formData) {
		this.formData = formData;
	}
}
