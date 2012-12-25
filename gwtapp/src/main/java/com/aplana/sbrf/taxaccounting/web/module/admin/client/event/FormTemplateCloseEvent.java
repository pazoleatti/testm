package com.aplana.sbrf.taxaccounting.web.module.admin.client.event;


import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

public class FormTemplateCloseEvent extends GwtEvent<FormTemplateCloseEvent.MyHandler> {

	public static interface MyHandler extends EventHandler {
		void onClose(FormTemplateCloseEvent event);
	}

	private static final Type<MyHandler> TYPE = new Type<MyHandler>();

	public static Type<MyHandler> getType() {
		return TYPE;
	}

	public FormTemplateCloseEvent() {
	}

	public static void fire(HasHandlers source) {
		source.fireEvent(new FormTemplateCloseEvent());
	}

	@Override
	protected void dispatch(MyHandler handler) {
		handler.onClose(this);
	}

	@Override
	public Type<MyHandler> getAssociatedType() {
		return getType();
	}

}
