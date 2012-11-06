package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/** @author Vitalii Samolovskikh */
public class SaveFormDataAction extends UnsecuredActionImpl<SaveFormDataResult> {
    private FormData formData;

    public SaveFormDataAction() {
    }

    public FormData getFormData() {
        return formData;
    }

    public void setFormData(FormData formData) {
        this.formData = formData;
    }
}
