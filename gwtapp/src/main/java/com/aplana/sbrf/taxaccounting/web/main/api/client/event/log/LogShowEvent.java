package com.aplana.sbrf.taxaccounting.web.main.api.client.event.log;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * Отобросить/скрыть панель с сообщениями
 * 
 * @author sgoryachkin
 * 
 */
public class LogShowEvent extends
		GwtEvent<LogShowEvent.MyHandler> {

	public static interface MyHandler extends EventHandler {
		/**
		 * @param event
		 */
		void onLogShow(LogShowEvent event);
	}

	private static final Type<MyHandler> TYPE = new Type<MyHandler>();

	public static Type<MyHandler> getType() {
		return TYPE;
	}

	public static void fire(HasHandlers source,	boolean show) {
		LogShowEvent event = new LogShowEvent();
		event.setShow(true);
		source.fireEvent(event);
	}

	private boolean show;

	public LogShowEvent() {
	}

	@Override
	protected void dispatch(MyHandler handler) {
		handler.onLogShow(this);
	}

	@Override
	public Type<MyHandler> getAssociatedType() {
		return getType();
	}

	public boolean isShow() {
		return show;
	}

	public void setShow(boolean show) {
		this.show = show;
	}


}
