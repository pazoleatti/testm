package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * @author Eugene Stetsenko
 */
public class UpdateTable extends
		GwtEvent<UpdateTable.UpdateTableHandler> {

	public static interface UpdateTableHandler extends EventHandler {
		/**
		 * @param event
		 */
		void onUpdateTable(UpdateTable event);
	}

	private static final Type<UpdateTableHandler> TYPE = new Type<UpdateTableHandler>();

	public static Type<UpdateTableHandler> getType() {
		return TYPE;
	}

	public static void fire(HasHandlers source) {
		UpdateTable event = new UpdateTable();
		source.fireEvent(event);
	}

	public UpdateTable() {
	}

	@Override
	protected void dispatch(UpdateTableHandler handler) {
		handler.onUpdateTable(this);
	}

	@Override
	public Type<UpdateTableHandler> getAssociatedType() {
		return getType();
	}
}
