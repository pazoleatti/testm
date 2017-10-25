package com.aplana.sbrf.taxaccounting.web.module.periods.shared;

import com.aplana.sbrf.taxaccounting.model.ReportPeriodViewModel;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class GetCorrectPeriodsResult implements Result {
    private List<ReportPeriodViewModel> reportPeriod;

    public List<ReportPeriodViewModel> getReportPeriod() {
        return reportPeriod;
    }

    public void setReportPeriod(List<ReportPeriodViewModel> reportPeriod) {
        this.reportPeriod = reportPeriod;
    }
}
