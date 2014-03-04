package com.aplana.sbrf.taxaccounting.web.widget.menu.client;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.MenuItem;

public class NotificationMenuItem extends MenuItem {

	int count = 0;
	String name;
	MainMenuPresenter menuPresenter;

	public NotificationMenuItem(String text, final MainMenuPresenter menuPresenter) {
		super(text, new Scheduler.ScheduledCommand() {
			@Override
			public void execute() {
				menuPresenter.showNotificationDialog();

			}
		});
		this.menuPresenter = menuPresenter;
		name = text;
	}

	public void setCount(int count) {
		this.count = count;
		setText(String.valueOf(count) + " " + name);
	}

	public int getCount() {
		return count;
	}
}
