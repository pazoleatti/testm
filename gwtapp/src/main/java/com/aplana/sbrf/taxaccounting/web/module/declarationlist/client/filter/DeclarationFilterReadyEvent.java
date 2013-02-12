package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.filter;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * Событие вызывается когда закончена инициализация DeclarationFilterPresenter
 * 
 * @author sgoryachkin
 *
 */
public class DeclarationFilterReadyEvent extends
		GwtEvent<DeclarationFilterReadyEvent.MyHandler> {
	
	public static interface MyHandler extends EventHandler {
		/**
		 * @param event
		 */
		void onFilterReady(DeclarationFilterReadyEvent event);
	}

	private static final Type<MyHandler> TYPE = new Type<MyHandler>();

	public static Type<MyHandler> getType() {
		return TYPE;
	}

	public static void fire(HasHandlers source) {
		source.fireEvent(new DeclarationFilterReadyEvent());
	}
	
	public DeclarationFilterReadyEvent() {
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
