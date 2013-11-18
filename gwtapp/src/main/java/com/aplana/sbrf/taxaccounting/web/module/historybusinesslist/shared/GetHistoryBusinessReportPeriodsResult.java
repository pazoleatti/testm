package com.aplana.sbrf.taxaccounting.web.module.historybusinesslist.shared;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

/**
 * User: avanteev
 */
public class GetHistoryBusinessReportPeriodsResult implements Result {
    private List<ReportPeriod> reportPeriodList;

    public List<ReportPeriod> getReportPeriodList() {
        return reportPeriodList;
    }

    public void setReportPeriodList(List<ReportPeriod> reportPeriodList) {
        this.reportPeriodList = reportPeriodList;
    }
}
