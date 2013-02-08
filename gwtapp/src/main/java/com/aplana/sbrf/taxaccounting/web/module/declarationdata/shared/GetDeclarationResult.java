package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.aplana.sbrf.taxaccounting.model.Declaration;
import com.gwtplatform.dispatch.shared.Result;

public class GetDeclarationResult implements Result {
    private Declaration declaration;

    public Declaration getDeclaration() {
        return declaration;
    }

    public void setDeclaration(Declaration declaration) {
        this.declaration = declaration;
    }
}
