package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;
import java.util.Set;

/**
 * @author Dmitriy Levykin
 */
public class GetTaxPeriodWDResult implements Result {

    private List<TaxPeriod> taxPeriods;

    private ReportPeriod lastReportPeriod;

    public List<TaxPeriod> getTaxPeriods() {
        return taxPeriods;
    }

    public void setTaxPeriods(List<TaxPeriod> taxPeriods) {
        this.taxPeriods = taxPeriods;
    }

    public ReportPeriod getLastReportPeriod() {
        return lastReportPeriod;
    }

    public void setLastReportPeriod(ReportPeriod lastReportPeriod) {
        this.lastReportPeriod = lastReportPeriod;
    }
}
