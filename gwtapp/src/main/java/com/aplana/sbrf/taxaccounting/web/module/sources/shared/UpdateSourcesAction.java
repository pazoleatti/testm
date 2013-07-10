package com.aplana.sbrf.taxaccounting.web.module.sources.shared;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplate;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class UpdateSourcesAction extends UnsecuredActionImpl<UpdateSourcesResult> {
    private DeclarationTemplate declaration;

    public DeclarationTemplate getDeclarationTemplate() {
        return declaration;
    }

    public void setDeclarationTemplate(DeclarationTemplate declaration) {
        this.declaration = declaration;
    }
}
