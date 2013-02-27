package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.events;

import com.google.gwt.event.shared.*;

public class FormDataListCreateEvent extends GwtEvent<FormDataListCreateEvent.FormDataCreateHandler> {

	public static interface FormDataCreateHandler extends EventHandler {
		void onFormDataCreateButtonClicked(FormDataListCreateEvent event);
	}

	private static final Type<FormDataCreateHandler> TYPE = new Type<FormDataCreateHandler>();

	public static Type<FormDataCreateHandler> getType() {
		return TYPE;
	}

	public static void fire(HasHandlers source) {
		source.fireEvent(new FormDataListCreateEvent());
	}

	public FormDataListCreateEvent() {
	}

	@Override
	protected void dispatch(FormDataCreateHandler handler) {
		handler.onFormDataCreateButtonClicked(this);
	}

	@Override
	public Type<FormDataCreateHandler> getAssociatedType() {
		return getType();
	}
}
