package com.aplana.sbrf.taxaccounting.web.module.admin.shared;

import com.aplana.sbrf.taxaccounting.model.Form;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

/**
 * @author Vitalii Samolovskikh
 */
public class FormListResult implements Result {
    private List<Form> forms;

    public FormListResult() {
    }

    public List<Form> getForms() {
        return forms;
    }

    public void setForms(List<Form> forms) {
        this.forms = forms;
    }
}
