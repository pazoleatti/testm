package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared;

import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class EditDeclarationTypeNameAction extends UnsecuredActionImpl<EditDeclarationTypeNameResult> {
    DeclarationType newDeclarationType;

    public DeclarationType getNewDeclarationType() {
        return newDeclarationType;
    }

    public void setNewDeclarationType(DeclarationType newDeclarationType) {
        this.newDeclarationType = newDeclarationType;
    }
}
