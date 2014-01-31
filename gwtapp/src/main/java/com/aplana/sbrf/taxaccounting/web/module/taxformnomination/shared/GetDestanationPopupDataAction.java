package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * @author auldanov
 */
public class GetDestanationPopupDataAction extends UnsecuredActionImpl<GetDestanationPopupDataResult> implements ActionName {

    private TaxType taxType;

    public TaxType getTaxType() {
        return taxType;
    }

    public void setTaxType(TaxType taxType) {
        this.taxType = taxType;
    }

    @Override
    public String getName() {
        return "Получение данных для заполнения модального окна назначений форм подразделениям";
    }
}
