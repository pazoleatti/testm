package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.creation;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.mvp.client.UiHandlers;


public interface DeclarationCreationUiHandlers extends UiHandlers {
	void onContinue();
	void onDepartmentChange();
    TaxType getTaxType();
    void onReportPeriodChange();
}
