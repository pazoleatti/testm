package com.aplana.sbrf.taxaccounting.web.widget.menu.shared;

import com.gwtplatform.dispatch.shared.Result;

import java.util.Date;

public class GetNotificationCountResult implements Result {
	private int notificationCount;
    private boolean editedRoles = false;
    private Date lastNotificationDate;

	public int getNotificationCount() {
		return notificationCount;
	}

	public void setNotificationCount(int notificationCount) {
		this.notificationCount = notificationCount;
	}

    public boolean isEditedRoles() {
        return editedRoles;
    }

    public void setEditedRoles(boolean editedRoles) {
        this.editedRoles = editedRoles;
    }

    public Date getLastNotificationDate() {
        return lastNotificationDate;
    }

    public void setLastNotificationDate(Date lastNotificationDate) {
        this.lastNotificationDate = lastNotificationDate;
    }
}
