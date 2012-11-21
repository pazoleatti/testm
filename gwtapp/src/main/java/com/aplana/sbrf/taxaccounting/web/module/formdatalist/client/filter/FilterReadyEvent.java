package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * Событие вызывается когда закончена инициализация FilterPresenter
 * 
 * @author sgoryachkin
 *
 */
public class FilterReadyEvent extends
		GwtEvent<FilterReadyEvent.MyHandler> {
	
	public static interface MyHandler extends EventHandler {
		/**
		 * @param event
		 */
		void onFilterReady(FilterReadyEvent event);
	}

	private static final Type<MyHandler> TYPE = new Type<MyHandler>();

	public static Type<MyHandler> getType() {
		return TYPE;
	}

	public static void fire(HasHandlers source) {
		source.fireEvent(new FilterReadyEvent());
	}
	
	public FilterReadyEvent() {
	}

	@Override
	protected void dispatch(MyHandler handler) {
		handler.onFilterReady(this);
	}

	@Override
	public Type<MyHandler> getAssociatedType() {
		return getType();
	}

}
