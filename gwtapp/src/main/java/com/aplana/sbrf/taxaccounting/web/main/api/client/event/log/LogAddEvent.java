package com.aplana.sbrf.taxaccounting.web.main.api.client.event.log;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * Добавляет сообщения в панель с сообщениями
 * 
 * @author sgoryachkin
 * @author Dmitriy Levykin
 *
 */
public class LogAddEvent extends
		GwtEvent<LogAddEvent.MyHandler> {

	public static interface MyHandler extends EventHandler {
		/**
		 * @param event
		 */
		void onLogUpdate(LogAddEvent event);
	}

	private static final Type<MyHandler> TYPE = new Type<MyHandler>();

	public static Type<MyHandler> getType() {
		return TYPE;
	}

	public static void fire(HasHandlers source,	String uuid) {
		LogAddEvent event = new LogAddEvent();
		event.setUuid(uuid);
		source.fireEvent(event);
	}

    /**
     * UUID-идентификатор списка сообщений
     */
	private String uuid;

	public LogAddEvent() {
	}

	@Override
	protected void dispatch(MyHandler handler) {
		handler.onLogUpdate(this);
	}

	@Override
	public Type<MyHandler> getAssociatedType() {
		return getType();
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
}
