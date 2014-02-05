package com.aplana.sbrf.taxaccounting.web.module.declarationversionlist.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * User: avanteev
 */
public class GetDTVersionListAction extends UnsecuredActionImpl<GetDTVersionListResult> {
    private int declarationFormTypeId;

    public int getDeclarationFormTypeId() {
        return declarationFormTypeId;
    }

    public void setDeclarationFormTypeId(int declarationFormTypeId) {
        this.declarationFormTypeId = declarationFormTypeId;
    }
}
