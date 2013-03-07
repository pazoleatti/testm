package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.event;


import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

public class FormTemplateFlushEvent extends GwtEvent<FormTemplateFlushEvent.MyHandler> {

	public static interface MyHandler extends EventHandler {
		void onFlush(FormTemplateFlushEvent event);
	}

	private static final Type<MyHandler> TYPE = new Type<MyHandler>();

	public static Type<MyHandler> getType() {
		return TYPE;
	}

	public FormTemplateFlushEvent() {
	}

	public static void fire(HasHandlers source) {
		source.fireEvent(new FormTemplateFlushEvent());
	}

	@Override
	protected void dispatch(MyHandler handler) {
		handler.onFlush(this);
	}

	@Override
	public Type<MyHandler> getAssociatedType() {
		return getType();
	}

}
