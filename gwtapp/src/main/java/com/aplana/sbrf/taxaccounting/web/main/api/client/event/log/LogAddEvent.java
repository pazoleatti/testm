package com.aplana.sbrf.taxaccounting.web.main.api.client.event.log;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * Добавляет сообщения в Очищает панель с сообщениями
 * 
 * @author sgoryachkin
 *
 */
public class LogAddEvent extends
		GwtEvent<LogAddEvent.MyHandler> {

	public static interface MyHandler extends EventHandler {
		/**
		 * @param event
		 */
		void onLogAdd(LogAddEvent event);
	}

	private static final Type<MyHandler> TYPE = new Type<MyHandler>();

	public static Type<MyHandler> getType() {
		return TYPE;
	}

	public static void fire(HasHandlers source,	List<LogEntry> logEntries) {
		LogAddEvent event = new LogAddEvent();
		event.setLogEntries(logEntries);
		source.fireEvent(event);
	}

	private List<LogEntry> logEntries;

	public LogAddEvent() {
	}

	@Override
	protected void dispatch(MyHandler handler) {
		handler.onLogAdd(this);
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
