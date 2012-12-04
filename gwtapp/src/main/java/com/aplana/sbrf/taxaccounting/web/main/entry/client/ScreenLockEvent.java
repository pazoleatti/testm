package com.aplana.sbrf.taxaccounting.web.main.entry.client;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * Событие вызывается когда закончена инициализация FilterPresenter
 * 
 * @author sgoryachkin
 * 
 */
public class ScreenLockEvent extends GwtEvent<ScreenLockEvent.MyHandler> {

	public static interface MyHandler extends EventHandler {
		/**
		 * @param event
		 */
		void onScreenLock(ScreenLockEvent event);
	}

	private static final Type<MyHandler> TYPE = new Type<MyHandler>();

	public static Type<MyHandler> getType() {
		return TYPE;
	}

	public static void fire(HasHandlers source, boolean lock) {
		ScreenLockEvent errorEvent = new ScreenLockEvent();
		errorEvent.setLock(lock);
		source.fireEvent(errorEvent);
	}

	private boolean lock;

	public ScreenLockEvent() {
	}

	@Override
	protected void dispatch(MyHandler handler) {
		handler.onScreenLock(this);
	}

	@Override
	public Type<MyHandler> getAssociatedType() {
		return getType();
	}

	public boolean isLock() {
		return lock;
	}

	public void setLock(boolean lock) {
		this.lock = lock;
	}



}
