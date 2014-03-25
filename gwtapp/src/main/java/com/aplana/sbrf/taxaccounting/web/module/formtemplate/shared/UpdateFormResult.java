package com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared;

import com.gwtplatform.dispatch.shared.Result;

/**
 * @author Vitalii Samolovskikh
 */
public class UpdateFormResult implements Result {
    private int formTemplateId;
    private String uuid;

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
