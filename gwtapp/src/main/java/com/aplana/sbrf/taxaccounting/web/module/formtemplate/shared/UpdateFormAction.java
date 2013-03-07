package com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * @author Vitalii Samolovskikh
 */
public class UpdateFormAction extends UnsecuredActionImpl<UpdateFormResult> {
    private FormTemplate form;

    public FormTemplate getForm() {
        return form;
    }

    public void setForm(FormTemplate form) {
        this.form = form;
    }
}
