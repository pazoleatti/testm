package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * Модеь запроса источников/приемников
 * @author auldanov
 */
public class SourcesAction extends UnsecuredActionImpl<SourcesResult> implements ActionName {
    private FormData formData;

    public FormData getFormData() {
        return formData;
    }

    public void setFormData(FormData formData) {
        this.formData = formData;
    }

    @Override
    public String getName() {
        return "Обработка запроса на получение источников/приемников формы";
    }
}
