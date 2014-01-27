package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

public class FormDestinationsDialogOpenEvent extends GwtEvent<FormDestinationsDialogOpenEvent.EditDestinationDialogOpenHandler> {

	public static interface EditDestinationDialogOpenHandler extends EventHandler {
		void onClickEditFormDestination(FormDestinationsDialogOpenEvent event);
	}

	private static final Type<EditDestinationDialogOpenHandler> TYPE = new Type<EditDestinationDialogOpenHandler>();

	public static Type<EditDestinationDialogOpenHandler> getType() {
		return TYPE;
	}

	public static void fire(HasHandlers source) {
		source.fireEvent(new FormDestinationsDialogOpenEvent());
	}

	public FormDestinationsDialogOpenEvent() {
	}

	@Override
	protected void dispatch(EditDestinationDialogOpenHandler handler) {
		handler.onClickEditFormDestination(this);
	}

	@Override
	public Type<EditDestinationDialogOpenHandler> getAssociatedType() {
		return getType();
	}
}
