package com.aplana.sbrf.taxaccounting.web.widget.menu.shared;

import com.gwtplatform.dispatch.shared.Result;

public class GetNotificationCountResult implements Result {
	private int notificationCount;
    private boolean editedRoles = false;

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
}
