package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter;

import com.aplana.sbrf.taxaccounting.model.*;
import com.gwtplatform.mvp.client.*;

public interface FilterUIHandlers extends UiHandlers {

	void onTaxPeriodSelected(TaxPeriod taxPeriod);

	TaxType getCurrentTaxType();

	void onCreateClicked();

	void onApplyClicked();
}
