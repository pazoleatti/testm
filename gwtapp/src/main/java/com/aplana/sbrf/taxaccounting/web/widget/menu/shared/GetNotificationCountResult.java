package com.aplana.sbrf.taxaccounting.web.widget.menu.shared;

import com.gwtplatform.dispatch.shared.Result;

public class GetNotificationCountResult implements Result {
	int notificationCount;

	public int getNotificationCount() {
		return notificationCount;
	}

	public void setNotificationCount(int notificationCount) {
		this.notificationCount = notificationCount;
	}
}
