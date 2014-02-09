package com.aplana.sbrf.taxaccounting.web.widget.menu.shared;

import java.util.List;

import com.aplana.sbrf.taxaccounting.web.widget.menu.client.NotificationMenuItem;
import com.gwtplatform.dispatch.shared.Result;

public class GetMainMenuResult implements Result{
	
	private List<MenuItem> menuItems;
	private String notificationMenuItemName;

	public List<MenuItem> getMenuItems() {
		return menuItems;
	}

	public void setMenuItems(List<MenuItem> menuItems) {
		this.menuItems = menuItems;
	}

	public String getNotificationMenuItemName() {
		return notificationMenuItemName;
	}

	public void setNotificationMenuItemName(String notificationMenuItemName) {
		this.notificationMenuItemName = notificationMenuItemName;
	}
}
