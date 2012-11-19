package com.aplana.sbrf.taxaccounting.web.module.admin.shared;

import com.aplana.sbrf.taxaccounting.model.Form;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * @author Vitalii Samolovskikh
 */
public class UpdateFormAction extends UnsecuredActionImpl<UpdateFormResult> {
    private Form form;

    public Form getForm() {
        return form;
    }

    public void setForm(Form form) {
        this.form = form;
    }
}
