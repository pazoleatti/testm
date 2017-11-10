package com.aplana.sbrf.taxaccounting.web.module.periods.shared;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class GetCorrectPeriodsResult implements Result {
    List<ReportPeriod> reportPeriod;

    public List<ReportPeriod> getReportPeriod() {
        return reportPeriod;
    }

    public void setReportPeriod(List<ReportPeriod> reportPeriod) {
        this.reportPeriod = reportPeriod;
    }
}
