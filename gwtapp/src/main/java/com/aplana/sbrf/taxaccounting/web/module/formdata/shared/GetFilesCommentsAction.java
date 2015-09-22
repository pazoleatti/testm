package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * Получение данных для формы "Параметры печатной формы"
 * @author dloshkarev
 */
public class GetFilesCommentsAction extends UnsecuredActionImpl<GetFilesCommentsResult> {
    private FormData formData;

    public FormData getFormData() {
        return formData;
    }

    public void setFormData(FormData formData) {
        this.formData = formData;
    }
}
