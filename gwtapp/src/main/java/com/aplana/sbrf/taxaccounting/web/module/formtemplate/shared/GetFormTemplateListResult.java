package com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.gwtplatform.dispatch.shared.Result;

/**
 * @author Vitalii Samolovskikh
 */
public class GetFormTemplateListResult implements Result {
    private List<FormTemplate> forms;

    public GetFormTemplateListResult() {
    }

    public List<FormTemplate> getForms() {
        return forms;
    }

    public void setForms(List<FormTemplate> forms) {
        this.forms = forms;
    }
}
