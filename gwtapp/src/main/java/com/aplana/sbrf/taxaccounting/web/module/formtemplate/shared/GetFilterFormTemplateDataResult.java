package com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared;

import com.aplana.sbrf.taxaccounting.model.*;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class GetFilterFormTemplateDataResult implements Result {
	private static final long serialVersionUID = -3436595935051736909L;

    List<TaxType> taxTypes;

    public List<TaxType> getTaxTypes() {
        return taxTypes;
    }

    public void setTaxTypes(List<TaxType> taxTypes) {
        this.taxTypes = taxTypes;
    }

}
