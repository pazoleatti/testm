package com.aplana.sbrf.taxaccounting.web.module.periods.client;

import com.aplana.sbrf.taxaccounting.web.module.periods.shared.TableRow;
import com.gwtplatform.mvp.client.UiHandlers;

public interface PeriodsUiHandlers extends UiHandlers {
	void closePeriod();
	void openPeriod();
	void find();
}
