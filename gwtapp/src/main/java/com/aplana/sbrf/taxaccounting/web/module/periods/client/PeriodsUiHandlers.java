package com.aplana.sbrf.taxaccounting.web.module.periods.client;

import com.aplana.sbrf.taxaccounting.web.module.periods.shared.TableRow;
import com.gwtplatform.mvp.client.UiHandlers;

public interface PeriodsUiHandlers extends UiHandlers {
	void applyFilter(int from, int to, long departmentId);
	void closePeriod(TableRow period);
	void openPeriod();
}
