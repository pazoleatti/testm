package com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class GetReportPeriodsResult implements Result {
	private static final long serialVersionUID = -2594530589463049810L;
	
	private List<ReportPeriod> reportPeriods;
    private ReportPeriod defaultReportPeriod;

	public List<ReportPeriod> getReportPeriods() {
		return reportPeriods;
	}

	public void setReportPeriods(List<ReportPeriod> reportPeriods) {
		this.reportPeriods = reportPeriods;
	}

    public ReportPeriod getDefaultReportPeriod() {
        return defaultReportPeriod;
    }

    public void setDefaultReportPeriod(ReportPeriod defaultReportPeriod) {
        this.defaultReportPeriod = defaultReportPeriod;
    }
}
