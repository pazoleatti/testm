package com.aplana.sbrf.taxaccounting.web.main.api.client.event;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * Добавляет сообщения в панель уведомлений
 */
public class NotificationsAddEvent extends
		GwtEvent<NotificationsAddEvent.MyHandler> {

	public static interface MyHandler extends EventHandler {
		/**
		 * @param event
		 */
		void onNotificationsAdd(NotificationsAddEvent event);
	}

	private static final Type<MyHandler> TYPE = new Type<MyHandler>();

	public static Type<MyHandler> getType() {
		return TYPE;
	}

	public static void fire(HasHandlers source,	List<LogEntry> logEntries) {
		NotificationsAddEvent event = new NotificationsAddEvent();
		event.setLogEntries(logEntries);
		source.fireEvent(event);
	}

	private List<LogEntry> logEntries;

	public NotificationsAddEvent() {
	}

	@Override
	protected void dispatch(MyHandler handler) {
		handler.onNotificationsAdd(this);
	}

	@Override
	public Type<MyHandler> getAssociatedType() {
		return getType();
	}

	public List<LogEntry> getLogEntries() {
		return logEntries;
	}

	public void setLogEntries(List<LogEntry> logEntries) {
		this.logEntries = logEntries;
	}

}
