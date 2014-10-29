package com.aplana.sbrf.taxaccounting.web.module.ifrs.shared;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

/**
 * @author lhaziev
 */
public class CreateIfrsDataResult implements Result {

    private List<ReportPeriod> reportPeriodList;

    public List<ReportPeriod> getReportPeriods() {
        return reportPeriodList;
    }

    public void setReportPeriods(List<ReportPeriod> reportPeriods) {
        this.reportPeriodList = reportPeriods;
    }
}
