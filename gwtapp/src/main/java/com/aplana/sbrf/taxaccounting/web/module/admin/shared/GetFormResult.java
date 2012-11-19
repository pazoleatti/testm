package com.aplana.sbrf.taxaccounting.web.module.admin.shared;

import com.aplana.sbrf.taxaccounting.model.Form;
import com.gwtplatform.dispatch.shared.Result;

/**
 * @author Vitalii Samolovskikh
 */
public class GetFormResult implements Result {
    private Form form;

    public Form getForm() {
        return form;
    }

    public void setForm(Form form) {
        this.form = form;
    }
}
