package com.aplana.sbrf.taxaccounting.web.module.periods.shared;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class RemovePeriodAction extends UnsecuredActionImpl<RemovePeriodResult> {
	TaxType taxType;
    int departmentReportPeriodId;

    public int getDepartmentReportPeriodId() {
        return departmentReportPeriodId;
    }

    public void setDepartmentReportPeriodId(int departmentReportPeriodId) {
        this.departmentReportPeriodId = departmentReportPeriodId;
    }

	public TaxType getTaxType() {
		return taxType;
	}

	public void setTaxType(TaxType taxType) {
		this.taxType = taxType;
	}
}
