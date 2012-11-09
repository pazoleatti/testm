package com.aplana.sbrf.taxaccounting.gwtapp.shared;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/** @author Vitalii Samolovskikh */
public class SaveDataAction extends UnsecuredActionImpl<SaveDataResult> {
    private FormData formData;

    public SaveDataAction() {
    }

    public FormData getFormData() {
        return formData;
    }

    public void setFormData(FormData formData) {
        this.formData = formData;
    }
}
