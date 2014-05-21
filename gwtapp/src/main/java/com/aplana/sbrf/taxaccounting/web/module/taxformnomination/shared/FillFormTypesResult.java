package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared;

import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

/**
 * @author vpetrov
 */
public class FillFormTypesResult implements Result {

    List<FormDataKind> formTypes;

    TaxType taxType;

    public TaxType getTaxType() {
        return taxType;
    }

    public void setTaxType(TaxType taxType) {
        this.taxType = taxType;
    }

    public List<FormDataKind> getFormTypes() {
        return formTypes;
    }

    public void setFormTypes(List<FormDataKind> formTypes) {
        this.formTypes = formTypes;
    }
}
