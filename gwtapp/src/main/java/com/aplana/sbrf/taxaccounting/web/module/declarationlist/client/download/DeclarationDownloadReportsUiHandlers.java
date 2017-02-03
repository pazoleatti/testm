package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.download;

import com.aplana.sbrf.taxaccounting.model.DeclarationFormKind;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.mvp.client.UiHandlers;


public interface DeclarationDownloadReportsUiHandlers extends UiHandlers {
	void onContinue();
	void onDepartmentChange();
    TaxType getTaxType();
    void onReportPeriodChange();
}
