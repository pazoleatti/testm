package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared;

import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.gwtplatform.dispatch.shared.Result;

/**
 * User: avanteev
 */
public class GetDeclarationTypeResult implements Result {
    private DeclarationType declarationType;

    public DeclarationType getDeclarationType() {
        return declarationType;
    }

    public void setDeclarationType(DeclarationType declarationType) {
        this.declarationType = declarationType;
    }
}
