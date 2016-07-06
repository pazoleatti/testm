package com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.gwtplatform.dispatch.shared.Result;

/**
 * @author Vitalii Samolovskikh
 */
public class UpdateFormResult implements Result {
    private int formTemplateId;
    private String uuid;
    private FormTemplate formTemplate;
    private boolean isConfirmNeeded;

    public boolean isConfirmNeeded() {
        return isConfirmNeeded;
    }

    public void setConfirmNeeded(boolean isConfirmNeeded) {
        this.isConfirmNeeded = isConfirmNeeded;
    }

    public FormTemplate getFormTemplate() {
        return formTemplate;
    }

    public void setFormTemplate(FormTemplate formTemplate) {
        this.formTemplate = formTemplate;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getFormTemplateId() {
        return formTemplateId;
    }

    public void setFormTemplateId(int formTemplateId) {
        this.formTemplateId = formTemplateId;
    }
}
