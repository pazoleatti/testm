package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;
/**
 * 
 * @author Eugene Stetsenko
 * Запрос для удаления формы
 *
 */
public class DeleteFormAction extends UnsecuredActionImpl<DeleteFormResult> {
	private long formDataId;
	
	public long getFormDataId() {
		return formDataId;
	}
	public void setFormDataId(long formDataId) {
		this.formDataId = formDataId;
	}

	
}
