package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;


/**
 * Запрос для заполнения полей формы создания назначения НФ.
 * @author vpetrov
 */
public class FillFormTypesAction extends UnsecuredActionImpl<FillFormTypesResult> {

    private TaxType taxType;

    public TaxType getTaxType() {
        return taxType;
    }

    public void setTaxType(TaxType taxType) {
        this.taxType = taxType;
    }
}
