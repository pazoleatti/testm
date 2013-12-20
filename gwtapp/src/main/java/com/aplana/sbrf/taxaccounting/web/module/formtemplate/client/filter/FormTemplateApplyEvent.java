package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.filter;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

public class FormTemplateApplyEvent extends GwtEvent<FormTemplateApplyEvent.FormDataApplyHandler> {

	public static interface FormDataApplyHandler extends EventHandler {
		void onClickFind(FormTemplateApplyEvent event);
	}

	private static final Type<FormDataApplyHandler> TYPE = new Type<FormDataApplyHandler>();

	public static Type<FormDataApplyHandler> getType() {
		return TYPE;
	}

	public static void fire(HasHandlers source) {
		source.fireEvent(new FormTemplateApplyEvent());
	}

	public FormTemplateApplyEvent() {
	}

	@Override
	protected void dispatch(FormDataApplyHandler handler) {
		handler.onClickFind(this);
	}

	@Override
	public Type<FormDataApplyHandler> getAssociatedType() {
		return getType();
	}
}
