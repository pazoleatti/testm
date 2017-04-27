package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared;


import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class GetTaxFormTypesResult implements Result {

    private List<DeclarationType> formTypeList;

    public List<DeclarationType> getFormTypeList() {
        return formTypeList;
    }

    public void setFormTypeList(List<DeclarationType> formTypeList) {
        this.formTypeList = formTypeList;
    }
}
