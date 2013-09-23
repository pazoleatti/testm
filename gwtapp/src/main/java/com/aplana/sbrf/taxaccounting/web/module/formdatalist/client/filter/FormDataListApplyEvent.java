package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter;

import com.google.gwt.event.shared.*;

public class FormDataListApplyEvent extends GwtEvent<FormDataListApplyEvent.FormDataApplyHandler> {

	public static interface FormDataApplyHandler extends EventHandler {
		void onClickFind(FormDataListApplyEvent event);
	}

	private static final Type<FormDataApplyHandler> TYPE = new Type<FormDataApplyHandler>();

	public static Type<FormDataApplyHandler> getType() {
		return TYPE;
	}

	public static void fire(HasHandlers source) {
		source.fireEvent(new FormDataListApplyEvent());
	}

	public FormDataListApplyEvent() {
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
