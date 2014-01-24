package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.filter;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

public class DeclarationTemplateCreateEvent extends GwtEvent<DeclarationTemplateCreateEvent.DTCreateHandler> {

	public static interface DTCreateHandler extends EventHandler {
		void onClickCreate(DeclarationTemplateCreateEvent event);
	}

	private static final Type<DTCreateHandler> TYPE = new Type<DTCreateHandler>();

	public static Type<DTCreateHandler> getType() {
		return TYPE;
	}

	public static void fire(HasHandlers source) {
		source.fireEvent(new DeclarationTemplateCreateEvent());
	}

	public DeclarationTemplateCreateEvent() {
	}

	@Override
	protected void dispatch(DTCreateHandler handler) {
		handler.onClickCreate(this);
	}

	@Override
	public Type<DTCreateHandler> getAssociatedType() {
		return getType();
	}
}
