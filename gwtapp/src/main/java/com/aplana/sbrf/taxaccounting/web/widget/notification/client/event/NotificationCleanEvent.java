package com.aplana.sbrf.taxaccounting.web.widget.notification.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * Очищает панель уведомлений
 */
public class NotificationCleanEvent extends
		GwtEvent<NotificationCleanEvent.MyHandler> {

	public static interface MyHandler extends EventHandler {
		/**
		 * @param event
		 */
		void onNotificationsClean(NotificationCleanEvent event);
	}

	private static final Type<MyHandler> TYPE = new Type<MyHandler>();

	public static Type<MyHandler> getType() {
		return TYPE;
	}

	public static void fire(HasHandlers source) {
		NotificationCleanEvent event = new NotificationCleanEvent();
		source.fireEvent(event);
	}

	public NotificationCleanEvent() {
	}

	@Override
	protected void dispatch(MyHandler handler) {
		handler.onNotificationsClean(this);
	}

	@Override
	public Type<MyHandler> getAssociatedType() {
		return getType();
	}

}
