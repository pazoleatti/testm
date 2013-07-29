package com.aplana.sbrf.taxaccounting.web.module.periods.client.opendialog;

import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.gwtplatform.mvp.client.UiHandlers;


public interface OpenDialogUiHandlers extends UiHandlers {
	void onContinue(OpenFilterData openFilterData);
	void onTaxPeriodSelected(TaxPeriod taxPeriod);
}
