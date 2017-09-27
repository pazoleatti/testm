package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class AddScriptAction extends UnsecuredActionImpl<AddScriptResult> {

    int formDataEventId;

    int declarationTemplateId;

    public int getFormDataEventId() {
        return formDataEventId;
    }

    public void setFormDataEventId(int formDataEventId) {
        this.formDataEventId = formDataEventId;
    }

    public int getDeclarationTemplateId() {
        return declarationTemplateId;
    }

    public void setDeclarationTemplateId(int declarationTemplateId) {
        this.declarationTemplateId = declarationTemplateId;
    }
}
