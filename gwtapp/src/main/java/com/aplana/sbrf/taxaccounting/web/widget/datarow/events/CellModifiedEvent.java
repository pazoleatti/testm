package com.aplana.sbrf.taxaccounting.web.widget.datarow.events;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.google.gwt.event.shared.GwtEvent;

public class CellModifiedEvent extends GwtEvent<CellModifiedEventHandler> {

	public static Type<CellModifiedEventHandler> TYPE = new Type<CellModifiedEventHandler>();

	private final DataRow<Cell> dataRow;

	public CellModifiedEvent(DataRow<Cell> dataRow) {
		this.dataRow = dataRow;
	}

	@Override
	public Type<CellModifiedEventHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(CellModifiedEventHandler handler) {
		handler.onCellModified(this);
	}

	public DataRow<Cell> getDataRow() {
		return dataRow;
	}
}
