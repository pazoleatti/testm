package com.aplana.sbrf.taxaccounting.web.module.declarationversionlist.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * User: avanteev
 */
public class GetDTVersionListAction extends UnsecuredActionImpl<GetDTVersionListResult> {
    private int declarationFormType;

    public int getDeclarationFormType() {
        return declarationFormType;
    }

    public void setDeclarationFormType(int declarationFormType) {
        this.declarationFormType = declarationFormType;
    }
}
