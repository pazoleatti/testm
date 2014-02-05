package com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared;

import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

/**
 * @author Vitalii Samolovskikh
 */
public class GetFormTemplateListResult implements Result {
    private List<FormTypeTemplate> formTypeTemplates;

    public List<FormTypeTemplate> getFormTypeTemplates() {
        return formTypeTemplates;
    }

    public void setFormTypeTemplates(List<FormTypeTemplate> formTypeTemplates) {
        this.formTypeTemplates = formTypeTemplates;
    }

}
