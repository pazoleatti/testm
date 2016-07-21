package com.aplana.sbrf.taxaccounting.web.widget.datarow.events;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.google.gwt.event.shared.GwtEvent;

public class CellEnteredEditModeEvent extends GwtEvent<CellEnteredEditModeEventHandler> {

    public static final Type<CellEnteredEditModeEventHandler> TYPE = new Type<CellEnteredEditModeEventHandler>();

    private final DataRow<Cell> dataRow;

    public CellEnteredEditModeEvent(DataRow<Cell> dataRow) {
        this.dataRow = dataRow;
    }

    @Override
    public Type<CellEnteredEditModeEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(CellEnteredEditModeEventHandler handler) {
        handler.onCellEnteredEditMode(this);
    }

    public DataRow<Cell> getDataRow() {
        return dataRow;
    }
}
