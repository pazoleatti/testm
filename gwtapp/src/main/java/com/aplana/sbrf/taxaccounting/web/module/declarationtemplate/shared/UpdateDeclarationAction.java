package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class UpdateDeclarationAction extends UnsecuredActionImpl<UpdateDeclarationResult> {
    private DeclarationTemplateExt declarationTemplateExt;
    private boolean force;

    public boolean getForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public DeclarationTemplateExt getDeclarationTemplateExt() {
        return declarationTemplateExt;
    }

    public void setDeclarationTemplateExt(DeclarationTemplateExt declarationTemplateExt) {
        this.declarationTemplateExt = declarationTemplateExt;
    }
}
