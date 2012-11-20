package com.aplana.sbrf.taxaccounting.web.module.admin.shared;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.gwtplatform.dispatch.shared.Result;

/**
 * @author Vitalii Samolovskikh
 */
public class FormListResult implements Result {
    private List<FormTemplate> forms;

    public FormListResult() {
    }

    public List<FormTemplate> getForms() {
        return forms;
    }

    public void setForms(List<FormTemplate> forms) {
        this.forms = forms;
    }
}
