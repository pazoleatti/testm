package com.aplana.sbrf.taxaccounting.web.module.audit.shared;

import com.aplana.sbrf.taxaccounting.model.LogSystemFilterAvailableValues;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

/**
 * User: avanteev
 * Date: 2013
 */
public class GetAuditFilterDataResult implements Result{

    private LogSystemFilterAvailableValues availableValues;
    private List<TaxType> taxTypes;

    public List<TaxType> getTaxTypes() {
        return taxTypes;
    }

    public void setTaxTypes(List<TaxType> taxTypes) {
        this.taxTypes = taxTypes;
    }

    public LogSystemFilterAvailableValues getAvailableValues() {
        return availableValues;
    }

    public void setAvailableValues(LogSystemFilterAvailableValues availableValues) {
        this.availableValues = availableValues;
    }
}
