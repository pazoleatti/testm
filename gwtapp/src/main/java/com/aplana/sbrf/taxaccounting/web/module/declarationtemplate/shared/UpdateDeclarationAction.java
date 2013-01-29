package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class UpdateDeclarationAction extends UnsecuredActionImpl<UpdateDeclarationResult> {
    private DeclarationTemplate declaration;

    public DeclarationTemplate getDeclarationTemplate() {
        return declaration;
    }

    public void setDeclarationTemplate(DeclarationTemplate declaration) {
        this.declaration = declaration;
    }
}
