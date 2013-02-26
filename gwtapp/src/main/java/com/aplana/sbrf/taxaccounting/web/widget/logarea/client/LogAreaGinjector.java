package com.aplana.sbrf.taxaccounting.web.widget.logarea.client;

import com.google.inject.Provider;

public interface LogAreaGinjector {

	Provider<LogAreaPresenter> getNotificationPresenter();
	
}