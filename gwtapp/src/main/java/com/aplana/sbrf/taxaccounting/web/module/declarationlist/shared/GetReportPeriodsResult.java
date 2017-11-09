package com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared;

import com.aplana.sbrf.taxaccounting.model.ReportPeriodViewModel;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class GetReportPeriodsResult implements Result {
	private static final long serialVersionUID = -2594530589463049810L;
	
	private List<ReportPeriodViewModel> reportPeriods;
    private ReportPeriodViewModel defaultReportPeriod;

	public List<ReportPeriodViewModel> getReportPeriods() {
		return reportPeriods;
	}

	public void setReportPeriods(List<ReportPeriodViewModel> reportPeriods) {
		this.reportPeriods = reportPeriods;
	}

    public ReportPeriodViewModel getDefaultReportPeriod() {
        return defaultReportPeriod;
    }

    public void setDefaultReportPeriod(ReportPeriodViewModel defaultReportPeriod) {
        this.defaultReportPeriod = defaultReportPeriod;
    }
}
