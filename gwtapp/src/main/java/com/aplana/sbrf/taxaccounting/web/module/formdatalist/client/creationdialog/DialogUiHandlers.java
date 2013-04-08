package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.creationdialog;


import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.gwtplatform.mvp.client.UiHandlers;

public interface DialogUiHandlers extends UiHandlers {
	void onConfirm();
	void onTaxPeriodSelected(TaxPeriod taxPeriod);
}
