package com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.gwtplatform.dispatch.shared.Result;

/**
 * User: avanteev
 * Результат после тестирования скрипта
 */
public class GetFormTestResult implements Result {
    private FormTemplate formTemplate;

    public FormTemplate getFormTemplate() {
        return formTemplate;
    }

    public void setFormTemplate(FormTemplate formTemplate) {
        this.formTemplate = formTemplate;
    }
}
