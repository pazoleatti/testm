package com.aplana.sbrf.taxaccounting.web.widget.datarow;

import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.events.CellModifiedEvent;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.events.CellModifiedEventHandler;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.cellview.client.Column;

/** @author Vitalii Samolovskikh */
public abstract class DataRowColumn<T> extends Column<DataRow<com.aplana.sbrf.taxaccounting.model.Cell>, T> implements HasHandlers {
    protected String alias;
	private HandlerManager handlerManager;
    
    protected DataRowColumn(Cell<T> cell, com.aplana.sbrf.taxaccounting.model.Column col) {
        super(cell);
	    handlerManager = new HandlerManager(this);
        this.alias = col.getAlias();
    }

	@Override
	public void fireEvent(GwtEvent<?> event) {
		handlerManager.fireEvent(event);
	}

	public HandlerRegistration addCellModifiedEventHandler(
			CellModifiedEventHandler handler) {
		return handlerManager.addHandler(CellModifiedEvent.TYPE, handler);
	}

    public String getAlias() {
        return alias;
    }
}
