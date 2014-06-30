package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class EditDeclarationTypeNameAction extends UnsecuredActionImpl<EditDeclarationTypeNameResult> {
    int declarationTypeId;
    String newDeclarationTypeName;

    public int getDeclarationTypeId() {
        return declarationTypeId;
    }

    public void setDeclarationTypeId(int declarationTypeId) {
        this.declarationTypeId = declarationTypeId;
    }

    public String getNewDeclarationTypeName() {
        return newDeclarationTypeName;
    }

    public void setNewDeclarationTypeName(String newDeclarationTypeName) {
        this.newDeclarationTypeName = newDeclarationTypeName;
    }
}
