package com.aplana.sbrf.taxaccounting.web.widget.notification.client;

import com.google.inject.Provider;

public interface NotificationGinjector {

	Provider<NotificationPresenter> getNotificationPresenter();
	
}