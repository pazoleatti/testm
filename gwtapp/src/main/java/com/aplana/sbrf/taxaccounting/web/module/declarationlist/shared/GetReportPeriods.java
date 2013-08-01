package com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared;

import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class GetReportPeriods extends UnsecuredActionImpl<GetReportPeriodsResult> {

	TaxPeriod taxPeriod;
    Integer departamentId;

	public TaxPeriod getTaxPeriod() {
		return taxPeriod;
	}

	public void setTaxPeriod(TaxPeriod taxPeriod) {
		this.taxPeriod = taxPeriod;
	}

    public Integer getDepartamentId() {
        return departamentId;
    }

    public void setDepartamentId(Integer departamentId) {
        this.departamentId = departamentId;
    }
}
