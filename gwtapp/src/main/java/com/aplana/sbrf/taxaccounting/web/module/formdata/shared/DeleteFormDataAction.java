package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;
/**
 * 
 * @author Eugene Stetsenko
 * Запрос для удаления формы
 *
 */
public class DeleteFormDataAction extends UnsecuredActionImpl<DeleteFormDataResult> implements ActionName {
	private long formDataId;
    private boolean manual;
	
	public long getFormDataId() {
		return formDataId;
	}
	public void setFormDataId(long formDataId) {
		this.formDataId = formDataId;
	}
	@Override
	public String getName() {
		return "Обработка запроса на удаление формы";
	}


    public boolean isManual() {
        return manual;
    }

    public void setManual(boolean manual) {
        this.manual = manual;
    }
}
