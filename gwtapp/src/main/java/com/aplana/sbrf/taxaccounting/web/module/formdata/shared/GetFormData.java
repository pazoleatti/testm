package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class GetFormData extends UnsecuredActionImpl<GetFormDataResult> implements ActionName {
	
	/**
	 * Старые данные формы для разблокировки
	 */
	private Long oldFormDataId;
	
	private Long formDataId;

	private boolean readOnly;

	public Long getFormDataId() {
		return formDataId;
	}

	public void setFormDataId(Long formDataId) {
		this.formDataId = formDataId;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean lockFormData) {
		this.readOnly = lockFormData;
	}

	public Long getOldFormDataId() {
		return oldFormDataId;
	}

	public void setOldFormDataId(Long oldFormDataId) {
		this.oldFormDataId = oldFormDataId;
	}
	

	@Override
	public String getName() {
		return "Получение налоговой формы";
	}


}
