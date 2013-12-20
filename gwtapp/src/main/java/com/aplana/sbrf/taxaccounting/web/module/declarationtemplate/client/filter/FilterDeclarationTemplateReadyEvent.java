package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.filter;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * Событие вызывается когда закончена инициализация FilterPresenter
 * 
 * @author Eugene Stetsenko
 *
 */
public class FilterDeclarationTemplateReadyEvent extends GwtEvent<FilterDeclarationTemplateReadyEvent.MyHandler> {
	
	public static interface MyHandler extends EventHandler {
		/**
		 * @param event
		 */
		void onFilterReady(FilterDeclarationTemplateReadyEvent event);
	}
	
	private boolean success;

	private static final Type<MyHandler> TYPE = new Type<MyHandler>();

	public static Type<MyHandler> getType() {
		return TYPE;
	}

	public static void fire(HasHandlers source, boolean success) {
		FilterDeclarationTemplateReadyEvent event = new FilterDeclarationTemplateReadyEvent();
		event.setSuccess(success);
		source.fireEvent(event);
	}
	
	public FilterDeclarationTemplateReadyEvent() {
	}

	@Override
	protected void dispatch(MyHandler handler) {
		handler.onFilterReady(this);

	}

	@Override
	public Type<MyHandler> getAssociatedType() {
		return getType();
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

}
