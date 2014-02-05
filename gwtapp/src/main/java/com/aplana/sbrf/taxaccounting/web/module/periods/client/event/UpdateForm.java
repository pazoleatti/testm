package com.aplana.sbrf.taxaccounting.web.module.periods.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * @author Eugene Stetsenko
 */
public class UpdateForm extends
		GwtEvent<UpdateForm.UpdateFormHandler> {

	public static interface UpdateFormHandler extends EventHandler {
		/**
		 * @param event
		 */
		void onUpdateFormHandler(UpdateForm event);
	}

	private static final Type<UpdateFormHandler> TYPE = new Type<UpdateFormHandler>();

	public static Type<UpdateFormHandler> getType() {
		return TYPE;
	}

	public static void fire(HasHandlers source) {
		UpdateForm event = new UpdateForm();
		source.fireEvent(event);
	}

	public UpdateForm() {
	}

	@Override
	protected void dispatch(UpdateFormHandler handler) {
		handler.onUpdateFormHandler(this);
	}

	@Override
	public Type<UpdateFormHandler> getAssociatedType() {
		return getType();
	}
}
