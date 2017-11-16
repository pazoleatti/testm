package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.creation;

import com.aplana.sbrf.taxaccounting.model.DeclarationFormKind;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.mvp.client.UiHandlers;

import java.util.List;


public interface DeclarationCreationUiHandlers extends UiHandlers {
	void onContinue();
	void onDepartmentChange();
    TaxType getTaxType();
    void onReportPeriodChange();
    List<DeclarationFormKind> getDeclarationFormKinds();
}
