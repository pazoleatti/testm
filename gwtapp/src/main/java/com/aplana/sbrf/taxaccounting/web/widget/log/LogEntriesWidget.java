package com.aplana.sbrf.taxaccounting.web.widget.log;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.web.widget.cell.LogEntryCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class LogEntriesWidget extends Composite implements LogEntriesView {

	private static LogEntryWidgetUiBinder uiBinder = GWT
			.create(LogEntryWidgetUiBinder.class);

	interface LogEntryWidgetUiBinder extends UiBinder<Widget, LogEntriesWidget> {
	}

	public LogEntriesWidget() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@UiField(provided = true)
	CellList<LogEntry> cellList = new CellList<LogEntry>(new LogEntryCell());

	@Override
	public void setLogEntries(List<LogEntry> entries) {
		if (entries != null) {
			cellList.setRowCount(entries.size());
			cellList.setRowData(entries);

		} else {
			cellList.setRowCount(0);
		}
		cellList.redraw();
	}

}
