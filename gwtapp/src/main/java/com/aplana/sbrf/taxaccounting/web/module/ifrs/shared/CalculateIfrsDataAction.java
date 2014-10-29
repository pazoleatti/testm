package com.aplana.sbrf.taxaccounting.web.module.ifrs.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * @author lhaziev
 */
public class CalculateIfrsDataAction extends UnsecuredActionImpl<CalculateIfrsDataResult> implements ActionName {

    private Integer reportPeriodId;

    public Integer getReportPeriodId() {
        return reportPeriodId;
    }

    public void setReportPeriodId(Integer reportPeriodId) {
        this.reportPeriodId = reportPeriodId;
    }

    @Override
    public String getName() {
        return "Сформировать отчетности для МСФО";
    }
}
