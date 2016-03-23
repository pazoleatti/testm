package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.NumericColumn;
import com.aplana.sbrf.taxaccounting.model.NumericColumnFormatter;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class GetFormDataAction extends UnsecuredActionImpl<GetFormDataResult> implements ActionName {
	
	/**
	 * Старые данные формы для разблокировки
	 */
	private Long oldFormDataId;
	
	private Long formDataId;

	private boolean readOnly;
    private boolean manual;
    private boolean correctionDiff;

    private String uuid;

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

    public boolean isManual() {
        return manual;
    }

    public void setManual(boolean manual) {
        this.manual = manual;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isCorrectionDiff() {
        return correctionDiff;
    }

    public void setCorrectionDiff(boolean correctionDiff) {
        this.correctionDiff = correctionDiff;
    }
}
