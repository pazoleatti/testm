package com.aplana.sbrf.taxaccounting.web.module.sources.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

public class EditDestinationDialogOpenEvent extends GwtEvent<EditDestinationDialogOpenEvent.EditDestinationDialogOpenHandler> {

	public static interface EditDestinationDialogOpenHandler extends EventHandler {
		void onClickEditDestination(EditDestinationDialogOpenEvent event);
	}

	private static final Type<EditDestinationDialogOpenHandler> TYPE = new Type<EditDestinationDialogOpenHandler>();

	public static Type<EditDestinationDialogOpenHandler> getType() {
		return TYPE;
	}

	public static void fire(HasHandlers source) {
		source.fireEvent(new EditDestinationDialogOpenEvent());
	}

	public EditDestinationDialogOpenEvent() {
	}

	@Override
	protected void dispatch(EditDestinationDialogOpenHandler handler) {
		handler.onClickEditDestination(this);
	}

	@Override
	public Type<EditDestinationDialogOpenHandler> getAssociatedType() {
		return getType();
	}
}
