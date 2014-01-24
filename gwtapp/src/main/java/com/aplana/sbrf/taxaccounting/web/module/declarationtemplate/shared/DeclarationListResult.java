package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared;

import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class DeclarationListResult implements Result {
    private List<DeclarationTypeTemplate> typeTemplates;

    public List<DeclarationTypeTemplate> getTypeTemplates() {
        return typeTemplates;
    }

    public void setTypeTemplates(List<DeclarationTypeTemplate> typeTemplates) {
        this.typeTemplates = typeTemplates;
    }

}
