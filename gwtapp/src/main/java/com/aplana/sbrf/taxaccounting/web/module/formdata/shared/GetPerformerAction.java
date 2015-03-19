package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * Получение данных для формы "Параметры печатной формы"
 * @author dloshkarev
 */
public class GetPerformerAction extends UnsecuredActionImpl<GetPerformerResult> {
    private FormData formData;

    public FormData getFormData() {
        return formData;
    }

    public void setFormData(FormData formData) {
        this.formData = formData;
    }
}
