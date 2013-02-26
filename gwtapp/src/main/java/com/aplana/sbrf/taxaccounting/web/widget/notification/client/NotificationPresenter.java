package com.aplana.sbrf.taxaccounting.web.widget.notification.client;

import java.util.ArrayList;
import java.util.List;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.web.widget.notification.client.event.NotificationAddEvent;
import com.aplana.sbrf.taxaccounting.web.widget.notification.client.event.NotificationCleanEvent;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class NotificationPresenter extends
		PresenterWidget<NotificationPresenter.MyView> implements
		NotificationAddEvent.MyHandler, NotificationCleanEvent.MyHandler, NotificationUiHandlers {

	
	public static interface MyView extends View, HasUiHandlers<NotificationUiHandlers>{
		
		void setLogEntries(List<LogEntry> entries);
		void setLogSize(int full, int error, int warn, int info);

	}
	
	private List<LogEntry> logEntries = new ArrayList<LogEntry>();

	@Inject
	public NotificationPresenter(final EventBus eventBus, final MyView view) {
		super(eventBus, view);
		getView().setUiHandlers(this);
	}
	
	@Override
	protected void onBind() {
		super.onBind();
		addRegisteredHandler(NotificationAddEvent.getType(), this);
		addRegisteredHandler(NotificationCleanEvent.getType(), this);
	}
	
	@Override
	public void onNotificationsAdd(NotificationAddEvent event) {
		logEntries.addAll(event.getLogEntries());
		updateView();
	}

	@Override
	public void onNotificationsClean(NotificationCleanEvent event) {
		logEntries.clear();
		updateView();
	}
	
	private void updateView(){
		getView().setLogEntries(logEntries);
		int error = 0, warn = 0, info = 0;
		for (LogEntry logEntry : logEntries) {
			switch (logEntry.getLevel()) {
			case ERROR:
				error++;
				break;
			case WARNING:
				warn++;
				break;
			case INFO:
				info++;
			}
		}
		getView().setLogSize(logEntries.size(), error, warn, info);
	}

	@Override
	public void print() {
		Window.alert("Не реализовано");	
	}

	@Override
	public void clean() {
		NotificationCleanEvent.fire(this);
	}

}
