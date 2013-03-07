package com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.gwtplatform.dispatch.shared.Result;

/**
 * @author Vitalii Samolovskikh
 */
public class GetFormResult implements Result {
    private FormTemplate form;

    public FormTemplate getForm() {
        return form;
    }

    public void setForm(FormTemplate form) {
        this.form = form;
    }
}
