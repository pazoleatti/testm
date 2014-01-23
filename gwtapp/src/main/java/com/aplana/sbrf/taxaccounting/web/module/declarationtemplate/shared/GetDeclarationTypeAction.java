package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * User: avanteev
 */
public class GetDeclarationTypeAction extends UnsecuredActionImpl<GetDeclarationTypeResult> {
    private int declarationTypeId;

    public int getDeclarationTypeId() {
        return declarationTypeId;
    }

    public void setDeclarationTypeId(int declarationTypeId) {
        this.declarationTypeId = declarationTypeId;
    }
}
