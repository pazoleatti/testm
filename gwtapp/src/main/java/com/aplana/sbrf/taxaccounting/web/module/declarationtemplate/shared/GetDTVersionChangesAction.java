package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * User: avanteev
 */
public class GetDTVersionChangesAction extends UnsecuredActionImpl<GetDTVersionChangesResult> {
    private int declarationTemplateId;

    public int getDeclarationTemplateId() {
        return declarationTemplateId;
    }

    public void setDeclarationTemplateId(int declarationTemplateId) {
        this.declarationTemplateId = declarationTemplateId;
    }
}
