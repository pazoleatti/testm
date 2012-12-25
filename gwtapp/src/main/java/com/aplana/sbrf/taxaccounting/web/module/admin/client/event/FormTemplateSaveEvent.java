package com.aplana.sbrf.taxaccounting.web.module.admin.client.event;


import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

public class FormTemplateSaveEvent extends GwtEvent<FormTemplateSaveEvent.MyHandler> {

	public static interface MyHandler extends EventHandler {
		void onSave(FormTemplateSaveEvent event);
	}

	private static final Type<MyHandler> TYPE = new Type<MyHandler>();

	public static Type<MyHandler> getType() {
		return TYPE;
	}

	public FormTemplateSaveEvent() {
	}

	public static void fire(HasHandlers source) {
		source.fireEvent(new FormTemplateSaveEvent());
	}

	@Override
	protected void dispatch(MyHandler handler) {
		handler.onSave(this);
	}

	@Override
	public Type<MyHandler> getAssociatedType() {
		return getType();
	}

}