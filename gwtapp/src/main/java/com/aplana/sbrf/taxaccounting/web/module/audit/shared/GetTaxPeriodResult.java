package com.aplana.sbrf.taxaccounting.web.module.audit.shared;

import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

/**
 * User: avanteev
 */
public class GetTaxPeriodResult implements Result {

    private List<TaxPeriod> taxPeriods;

    public List<TaxPeriod> getTaxPeriods() {
        return taxPeriods;
    }

    public void setTaxPeriods(List<TaxPeriod> taxPeriods) {
        this.taxPeriods = taxPeriods;
    }
}
