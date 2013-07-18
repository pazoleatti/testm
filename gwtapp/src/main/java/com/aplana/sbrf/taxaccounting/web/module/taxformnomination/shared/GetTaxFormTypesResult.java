package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared;


import com.aplana.sbrf.taxaccounting.model.FormType;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class GetTaxFormTypesResult implements Result {

    private List<FormType> formTypeList;


    public List<FormType> getFormTypeList() {
        return formTypeList;
    }

    public void setFormTypeList(List<FormType> formTypeList) {
        this.formTypeList = formTypeList;
    }
}
