package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;
/**
 * 
 * @author Eugene Stetsenko
 * Запрос для получения доступных переходов между этапами
 *
 */
public class GetAvailableMovesAction extends UnsecuredActionImpl<GetAvailableMovesResult> {
	private long formDataId;
	
	public long getFormDataId() {
		return formDataId;
	}
	public void setFormDataId(long formDataId) {
		this.formDataId = formDataId;
	}

	
}
