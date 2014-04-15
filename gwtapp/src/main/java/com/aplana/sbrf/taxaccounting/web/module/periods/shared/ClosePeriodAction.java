package com.aplana.sbrf.taxaccounting.web.module.periods.shared;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.Date;

public class ClosePeriodAction extends UnsecuredActionImpl<ClosePeriodResult> {
	TaxType taxType;
	int reportPeriodId;
	long departmentId;
    Date correctionDate;

	public TaxType getTaxType() {
		return taxType;
	}

	public void setTaxType(TaxType taxType) {
		this.taxType = taxType;
	}

	public int getReportPeriodId() {
		return reportPeriodId;
	}

	public void setReportPeriodId(int reportPeriodId) {
		this.reportPeriodId = reportPeriodId;
	}

	public long getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(long departmentId) {
		this.departmentId = departmentId;
	}

    public Date getCorrectionDate() {
        return correctionDate;
    }

    public void setCorrectionDate(Date correctionDate) {
        this.correctionDate = correctionDate;
    }
}
