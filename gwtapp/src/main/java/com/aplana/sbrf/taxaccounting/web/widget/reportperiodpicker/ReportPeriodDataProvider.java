package com.aplana.sbrf.taxaccounting.web.widget.reportperiodpicker;

import com.aplana.sbrf.taxaccounting.model.TaxPeriod;

public interface ReportPeriodDataProvider {

	//Вызов данной функции говорит о том, что у нас был выбран определенный TaxPeriod, и что для этого Налогового периода нужно
	//подгрузить список отчетных периодов и засетить их в виджет вызвав функцию ReportPeriodPicker#setReportPeriods(List<ReportPeriod>)
	void onTaxPeriodSelected(TaxPeriod taxPeriod);

}
