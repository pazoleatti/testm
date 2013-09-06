package com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared;

import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

@Deprecated
// TODO Какова... это акшен используется в куче форм
public class GetReportPeriods extends UnsecuredActionImpl<GetReportPeriodsResult> {

	TaxPeriod taxPeriod;
    Integer departmentId;

	public TaxPeriod getTaxPeriod() {
		return taxPeriod;
	}

	public void setTaxPeriod(TaxPeriod taxPeriod) {
		this.taxPeriod = taxPeriod;
	}

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }
}
