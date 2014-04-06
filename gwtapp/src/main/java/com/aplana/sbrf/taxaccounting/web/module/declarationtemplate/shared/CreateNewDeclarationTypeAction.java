package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * User: avanteev
 */
public class CreateNewDeclarationTypeAction extends UnsecuredActionImpl<CreateNewDeclarationTypeResult> {
    private DeclarationTemplateExt declarationTemplateExt;

    public DeclarationTemplateExt getDeclarationTemplateExt() {
        return declarationTemplateExt;
    }

    public void setDeclarationTemplateExt(DeclarationTemplateExt declarationTemplateExt) {
        this.declarationTemplateExt = declarationTemplateExt;
    }


}
