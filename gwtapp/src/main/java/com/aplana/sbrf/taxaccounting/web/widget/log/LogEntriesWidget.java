package com.aplana.sbrf.taxaccounting.web.widget.log;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.web.widget.log.cell.LogEntryIndexCell;
import com.aplana.sbrf.taxaccounting.web.widget.log.cell.LogEntryMessageCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class LogEntriesWidget extends Composite implements LogEntriesView {

	private static LogEntryWidgetUiBinder uiBinder = GWT
			.create(LogEntryWidgetUiBinder.class);

	interface LogEntryWidgetUiBinder extends UiBinder<Widget, LogEntriesWidget> {
	}

	@UiField
	CellTable<LogEntry> logCellTable;

	public LogEntriesWidget() {
		initWidget(uiBinder.createAndBindUi(this));

		Column<LogEntry, LogEntry> messageColumn = new Column<LogEntry, LogEntry>(
				new LogEntryMessageCell()) {
			@Override
			public LogEntry getValue(LogEntry object) {
				return object;
			}
		};

		Column<LogEntry, LogEntry> indexColumn = new Column<LogEntry, LogEntry>(
				new LogEntryIndexCell()) {
			@Override
			public LogEntry getValue(LogEntry object) {
				return object;
			}
		};

		logCellTable.addColumn(indexColumn);
		logCellTable.setColumnWidth(indexColumn, "30px");
		
		logCellTable.addColumn(messageColumn);
		setLogEntries(null);
	}

	@Override
	public void setLogEntries(List<LogEntry> entries) {
		if (entries != null) {
			logCellTable.setRowCount(entries.size());
			logCellTable.setRowData(entries);
		} else {
			logCellTable.setRowCount(0);
		}
		logCellTable.redraw();
	}

}
