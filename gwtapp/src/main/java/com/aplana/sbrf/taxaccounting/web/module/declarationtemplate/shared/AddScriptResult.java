package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplateEventScript;
import com.gwtplatform.dispatch.shared.Result;

public class AddScriptResult implements Result {

    DeclarationTemplateEventScript declarationTemplateEventScript;

    public DeclarationTemplateEventScript getDeclarationTemplateEventScript() {
        return declarationTemplateEventScript;
    }

    public void setDeclarationTemplateEventScript(DeclarationTemplateEventScript declarationTemplateEventScript) {
        this.declarationTemplateEventScript = declarationTemplateEventScript;
    }
}
