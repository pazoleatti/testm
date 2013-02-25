package com.aplana.sbrf.taxaccounting.web.widget.notification.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * Очищает панель уведомлений
 */
public class NotificationsCleanEvent extends
		GwtEvent<NotificationsCleanEvent.MyHandler> {

	public static interface MyHandler extends EventHandler {
		/**
		 * @param event
		 */
		void onNotificationsAdd(NotificationsCleanEvent event);
	}

	private static final Type<MyHandler> TYPE = new Type<MyHandler>();

	public static Type<MyHandler> getType() {
		return TYPE;
	}

	public static void fire(HasHandlers source) {
		NotificationsCleanEvent event = new NotificationsCleanEvent();
		source.fireEvent(event);
	}

	public NotificationsCleanEvent() {
	}

	@Override
	protected void dispatch(MyHandler handler) {
		handler.onNotificationsAdd(this);
	}

	@Override
	public Type<MyHandler> getAssociatedType() {
		return getType();
	}

}
