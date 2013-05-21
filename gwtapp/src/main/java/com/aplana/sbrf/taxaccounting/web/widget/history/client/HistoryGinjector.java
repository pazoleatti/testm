package com.aplana.sbrf.taxaccounting.web.widget.history.client;

import com.google.inject.Provider;

public interface HistoryGinjector {

	Provider<HistoryPresenter> getHistoryPresenter();
	
}