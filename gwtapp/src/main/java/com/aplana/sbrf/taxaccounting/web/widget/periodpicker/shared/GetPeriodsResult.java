package com.aplana.sbrf.taxaccounting.web.widget.periodpicker.shared;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.gwtplatform.dispatch.shared.Result;


public class GetPeriodsResult implements Result {
	private static final long serialVersionUID = 1099858218534060155L;
	
	private List<ReportPeriod> reportPeriods;

	public List<ReportPeriod> getReportPeriods() {
		return reportPeriods;
	}

	public void setReportPeriods(List<ReportPeriod> reportPeriods) {
		this.reportPeriods = reportPeriods;
	}


}
