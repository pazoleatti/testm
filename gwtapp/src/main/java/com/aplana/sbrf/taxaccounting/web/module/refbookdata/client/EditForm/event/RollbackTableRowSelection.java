package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * @author Eugene Stetsenko
 */
public class RollbackTableRowSelection extends
		GwtEvent<RollbackTableRowSelection.RollbackTableRowSelectionHandler> {

	public static interface RollbackTableRowSelectionHandler extends EventHandler {
		/**
		 * @param event
		 */
		void onRollbackTableRowSelection(RollbackTableRowSelection event);
	}

	private static final Type<RollbackTableRowSelectionHandler> TYPE = new Type<RollbackTableRowSelectionHandler>();

	public static Type<RollbackTableRowSelectionHandler> getType() {
		return TYPE;
	}

	public static void fire(HasHandlers source, Long recordId) {
		RollbackTableRowSelection event = new RollbackTableRowSelection();
		event.setRecordId(recordId);
		source.fireEvent(event);
	}

	private Long recordId;

	public RollbackTableRowSelection() {
	}

	@Override
	protected void dispatch(RollbackTableRowSelectionHandler handler) {
		handler.onRollbackTableRowSelection(this);
	}

	@Override
	public Type<RollbackTableRowSelectionHandler> getAssociatedType() {
		return getType();
	}

	public Long getRecordId() {
		return recordId;
	}

	public void setRecordId(Long recordId) {
		this.recordId = recordId;
	}
}