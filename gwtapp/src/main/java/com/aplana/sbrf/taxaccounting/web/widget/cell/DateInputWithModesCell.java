package com.aplana.sbrf.taxaccounting.web.widget.cell;

import com.aplana.sbrf.taxaccounting.model.*;
import com.google.gwt.cell.client.*;
import com.google.gwt.dom.client.*;

import java.util.*;

public class DateInputWithModesCell extends DateInputCell {

	ColumnContext columnContext;

	public DateInputWithModesCell(ColumnContext columnContext) {
		this.columnContext = columnContext;
	}

	@Override
	public void onBrowserEvent(Context context, Element parent, Date value,
	                           NativeEvent event, ValueUpdater<Date> valueUpdater) {
		DataRow dataRow = (DataRow)context.getKey();
		if ((columnContext.getMode() == ColumnContext.Mode.EDIT_MODE) ||
				((columnContext.getMode() != ColumnContext.Mode.READONLY_MODE) &&
						dataRow.getCell(columnContext.getColumn().getAlias()).isEditable())) {
			super.onBrowserEvent(context, parent, value, event, valueUpdater);
		}
	}
}
