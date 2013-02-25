package com.aplana.sbrf.taxaccounting.web.widget.notification.client;

import java.util.ArrayList;
import java.util.List;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.web.widget.notification.client.event.NotificationAddEvent;
import com.aplana.sbrf.taxaccounting.web.widget.notification.client.event.NotificationCleanEvent;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;

public class NotificationPresenter extends
		PresenterWidget<NotificationPresenter.MyView> implements
		NotificationAddEvent.MyHandler, NotificationCleanEvent.MyHandler {

	
	public static interface MyView extends View {
		
		void setLogEntries(List<LogEntry> entries);
		void setLogSize(int size);

	}
	
	private List<LogEntry> logEntries = new ArrayList<LogEntry>();

	@Inject
	public NotificationPresenter(final EventBus eventBus, final MyView view) {
		super(eventBus, view);
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
	@ProxyEvent
	public void onNotificationsClean(NotificationCleanEvent event) {
		logEntries.clear();
		updateView();
	}
	
	private void updateView(){
		getView().setLogEntries(logEntries);
		getView().setLogSize(logEntries.size());
	}

}
