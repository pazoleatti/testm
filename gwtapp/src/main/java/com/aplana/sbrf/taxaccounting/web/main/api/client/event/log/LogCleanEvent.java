package com.aplana.sbrf.taxaccounting.web.main.api.client.event.log;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * Очищает панель с сообщениями
 *  
 * @author sgoryachkin
 *
 */
public class LogCleanEvent extends
		GwtEvent<LogCleanEvent.MyHandler> {

	public static interface MyHandler extends EventHandler {
		/**
		 * @param event
		 */
		void onLogClean(LogCleanEvent event);
	}

	private static final Type<MyHandler> TYPE = new Type<MyHandler>();

	public static Type<MyHandler> getType() {
		return TYPE;
	}

	public static void fire(HasHandlers source) {
		LogCleanEvent event = new LogCleanEvent();
		source.fireEvent(event);
	}

	public LogCleanEvent() {
	}

	@Override
	protected void dispatch(MyHandler handler) {
		handler.onLogClean(this);
	}

	@Override
	public Type<MyHandler> getAssociatedType() {
		return getType();
	}

}
