package com.aplana.sbrf.taxaccounting.web.widget.reportperiodpicker;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;

import java.util.Map;

public interface ReportPeriodSelectHandler {

    /**
     * Выбор налогового периода
     * @param taxPeriod
     */
	void onTaxPeriodSelected(TaxPeriod taxPeriod);

    /**
     * Выбор отчетного периода
     */
    void onReportPeriodsSelected(Map<Integer, ReportPeriod> selectedReportPeriods);
}
