package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.filter;

import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.mvp.client.UiHandlers;

public interface DeclarationFilterUIHandlers extends UiHandlers {

	void onTaxPeriodSelected(TaxPeriod taxPeriod, Integer departmentId);

	TaxType getCurrentTaxType();
}
