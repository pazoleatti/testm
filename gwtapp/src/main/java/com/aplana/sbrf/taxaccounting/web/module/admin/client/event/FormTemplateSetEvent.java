package com.aplana.sbrf.taxaccounting.web.module.admin.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;


public class FormTemplateSetEvent extends GwtEvent<FormTemplateSetEvent.MyHandler> {

	public static interface MyHandler extends EventHandler {
		void onSet(FormTemplateSetEvent event);
	}

	private static final Type<MyHandler> TYPE = new Type<MyHandler>();

	public static Type<MyHandler> getType() {
		return TYPE;
	}

	public FormTemplateSetEvent() {
	}

	public static void fire(HasHandlers source) {
		source.fireEvent(new FormTemplateSetEvent());
	}

	@Override
	protected void dispatch(MyHandler handler) {
		handler.onSet(this);
	}

	@Override
	public Type<MyHandler> getAssociatedType() {
		return getType();
	}

}
