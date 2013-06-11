package com.aplana.sbrf.taxaccounting.web.module.periods.client;

import com.gwtplatform.mvp.client.UiHandlers;

public interface PeriodsUiHandlers extends UiHandlers {
	void applyFilter(int from, int to);
	void closePeriod(long periodId);
}
