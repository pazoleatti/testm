package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.gwtplatform.dispatch.shared.Result;

public class DeclarationListResult implements Result {
    private List<DeclarationTemplate> declarations;

    public DeclarationListResult() {
    }

    public List<DeclarationTemplate> getDeclarations() {
        return declarations;
    }

    public void setDeclarations(List<DeclarationTemplate> declarations) {
        this.declarations =  declarations;
    }
}
