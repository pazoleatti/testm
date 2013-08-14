package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.creation;

import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.gwtplatform.mvp.client.UiHandlers;


public interface DeclarationCreationUiHandlers extends UiHandlers {
	void onContinue();
	void onTaxPeriodSelected(TaxPeriod taxPeriod, Integer departmentId);
}
