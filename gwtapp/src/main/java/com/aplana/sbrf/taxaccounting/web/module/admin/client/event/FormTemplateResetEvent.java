package com.aplana.sbrf.taxaccounting.web.module.admin.client.event;


import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

public class FormTemplateResetEvent extends GwtEvent<FormTemplateResetEvent.MyHandler> {

	public static interface MyHandler extends EventHandler {
		void onReset(FormTemplateResetEvent event);
	}

	private static final Type<MyHandler> TYPE = new Type<MyHandler>();

	public static Type<MyHandler> getType() {
		return TYPE;
	}

	public FormTemplateResetEvent() {
	}

	public static void fire(HasHandlers source) {
		source.fireEvent(new FormTemplateResetEvent());
	}

	@Override
	protected void dispatch(MyHandler handler) {
		handler.onReset(this);
	}

	@Override
	public Type<MyHandler> getAssociatedType() {
		return getType();
	}

}