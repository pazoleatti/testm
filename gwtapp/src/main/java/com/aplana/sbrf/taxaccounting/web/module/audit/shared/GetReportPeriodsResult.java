package com.aplana.sbrf.taxaccounting.web.module.audit.shared;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

/**
 * User: avanteev
 */
public class GetReportPeriodsResult implements Result {

    private List<ReportPeriod> reportPeriodList;

    public List<ReportPeriod> getReportPeriods() {
        return reportPeriodList;
    }

    public void setReportPeriods(List<ReportPeriod> taxPeriods) {
        this.reportPeriodList = taxPeriods;
    }
}
