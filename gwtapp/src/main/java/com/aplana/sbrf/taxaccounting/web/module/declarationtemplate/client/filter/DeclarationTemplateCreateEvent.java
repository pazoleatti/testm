package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.filter;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

public class DeclarationTemplateCreateEvent extends GwtEvent<DeclarationTemplateCreateEvent.FormDataCreateHandler> {

	public static interface FormDataCreateHandler extends EventHandler {
		void onClickCreate(DeclarationTemplateCreateEvent event);
	}

	private static final Type<FormDataCreateHandler> TYPE = new Type<FormDataCreateHandler>();

	public static Type<FormDataCreateHandler> getType() {
		return TYPE;
	}

	public static void fire(HasHandlers source) {
		source.fireEvent(new DeclarationTemplateCreateEvent());
	}

	public DeclarationTemplateCreateEvent() {
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
