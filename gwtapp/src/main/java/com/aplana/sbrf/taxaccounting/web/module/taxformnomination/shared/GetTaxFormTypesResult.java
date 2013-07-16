package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared;


import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class GetTaxFormTypesResult implements Result {

    private List<FormType> formTypeList;
    //private List<DeclarationType> declarationTypeList;


    public List<FormType> getFormTypeList() {
        return formTypeList;
    }

    public void setFormTypeList(List<FormType> formTypeList) {
        this.formTypeList = formTypeList;
    }

    /*public List<DeclarationType> getDeclarationTypeList() {
        return declarationTypeList;
    }

    public void setDeclarationTypeList(List<DeclarationType> declarationTypeList) {
        this.declarationTypeList = declarationTypeList;
    }  */
}
