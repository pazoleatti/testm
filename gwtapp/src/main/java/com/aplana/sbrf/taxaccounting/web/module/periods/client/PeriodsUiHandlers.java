package com.aplana.sbrf.taxaccounting.web.module.periods.client;

import com.gwtplatform.mvp.client.UiHandlers;

public interface PeriodsUiHandlers extends UiHandlers {
	void closePeriod();
	void openPeriod();
    void openCorrectPeriod();
	void onFindButton();
    void setDeadline();
	void removePeriod();
	void selectionChanged();
    void editPeriod();
}
