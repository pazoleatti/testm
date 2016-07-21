package com.aplana.sbrf.taxaccounting.web.widget.cell;

import com.aplana.sbrf.taxaccounting.web.widget.datarow.events.CellEnteredEditModeEventHandler;
import com.google.gwt.event.shared.HandlerRegistration;

public interface HasCellEnteredEditModeEvent {
    HandlerRegistration addCellEnteredEditModeEventHandler(CellEnteredEditModeEventHandler handler);
}
