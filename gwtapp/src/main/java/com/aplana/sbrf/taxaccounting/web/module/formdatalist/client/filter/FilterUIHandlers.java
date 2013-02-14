package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter;

import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.mvp.client.UiHandlers;

public interface FilterUIHandlers extends UiHandlers {

	void onTaxPeriodSelected(TaxPeriod taxPeriod);

	TaxType getCurrentTaxType();
}
