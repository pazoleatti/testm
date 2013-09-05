package com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class GetReportPeriodsResult implements Result {
	private static final long serialVersionUID = -2594530589463049810L;
	
	List<ReportPeriod> reportPeriods;

	public List<ReportPeriod> getReportPeriods() {
		return reportPeriods;
	}

	public void setReportPeriods(List<ReportPeriod> reportPeriods) {
		this.reportPeriods = reportPeriods;
	}
}
