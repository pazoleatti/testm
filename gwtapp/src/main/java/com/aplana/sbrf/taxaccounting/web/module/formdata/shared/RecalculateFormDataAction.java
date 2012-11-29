package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.log.Logger;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;
/**
 * 
 * @author Eugene Stetsenko
 * Запрос для пересчета формы
 *
 */
public class RecalculateFormDataAction extends UnsecuredActionImpl<RecalculateFormDataResult> {
	private Long formDataId;
	
	public Long getFormDataId() {
		return formDataId;
	}
	public void setFormDataId(Long formDataId) {
		this.formDataId = formDataId;
	}
}
