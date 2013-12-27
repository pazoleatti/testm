package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.filter;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

public class FormTemplateCreateEvent extends GwtEvent<FormTemplateCreateEvent.FormDataCreateHandler> {

	public static interface FormDataCreateHandler extends EventHandler {
		void onClickCreate(FormTemplateCreateEvent event);
	}

	private static final Type<FormDataCreateHandler> TYPE = new Type<FormDataCreateHandler>();

	public static Type<FormDataCreateHandler> getType() {
		return TYPE;
	}

	public static void fire(HasHandlers source) {
		source.fireEvent(new FormTemplateCreateEvent());
	}

	public FormTemplateCreateEvent() {
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
