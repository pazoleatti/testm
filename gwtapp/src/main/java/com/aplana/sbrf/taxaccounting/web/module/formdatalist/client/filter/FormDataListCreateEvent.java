package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter;

import com.google.gwt.event.shared.*;

public class FormDataListCreateEvent extends GwtEvent<FormDataListCreateEvent.FormDataCreateHandler> {

	public interface FormDataCreateHandler extends EventHandler {
		void onClickCreate(FormDataListCreateEvent event);
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
		handler.onClickCreate(this);
	}

	@Override
	public Type<FormDataCreateHandler> getAssociatedType() {
		return getType();
	}
}
