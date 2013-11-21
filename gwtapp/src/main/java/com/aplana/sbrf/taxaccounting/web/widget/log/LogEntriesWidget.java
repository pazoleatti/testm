package com.aplana.sbrf.taxaccounting.web.widget.log;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.web.widget.log.cell.LogEntryImageCell;
import com.aplana.sbrf.taxaccounting.web.widget.log.cell.LogEntryIndexCell;
import com.aplana.sbrf.taxaccounting.web.widget.log.cell.LogEntryMessageCell;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericCellTable;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

import java.util.ArrayList;
import java.util.List;

public class LogEntriesWidget extends Composite implements LogEntriesView {

	private static LogEntryWidgetUiBinder uiBinder = GWT
			.create(LogEntryWidgetUiBinder.class);

	interface LogEntryWidgetUiBinder extends UiBinder<Widget, LogEntriesWidget> {
	}

	@UiField
    CellTable<LogEntry> logCellTable;

    @UiField
    FlexiblePager pager;

    @UiField
    DockLayoutPanel dock;

    private static final int PAGE_SIZE = 50;

    private ListDataProvider<LogEntry> dataProvider;

	public LogEntriesWidget() {
		initWidget(uiBinder.createAndBindUi(this));

		Column<LogEntry, LogEntry> messageColumn = new Column<LogEntry, LogEntry>(
				new LogEntryMessageCell()) {
			@Override
			public LogEntry getValue(LogEntry object) {
				return object;
			}
		};
		
		Column<LogEntry, LogEntry> imageColumn = new Column<LogEntry, LogEntry>(
				new LogEntryImageCell()) {
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
		logCellTable.addColumn(imageColumn);
		logCellTable.addColumn(messageColumn);

        dataProvider = new ListDataProvider<LogEntry>();
        dataProvider.addDataDisplay(logCellTable);
        pager.setDisplay(logCellTable);
        logCellTable.setPageSize(PAGE_SIZE);

        setLogEntries(null);
	}

	@Override
	public void setLogEntries(List<LogEntry> entries) {
        if (entries != null) {
            dataProvider.setList(entries);
        } else {
            dataProvider.setList(new ArrayList<LogEntry>());
        }
        boolean isVisible= entries != null && entries.size() > PAGE_SIZE;
        pager.setVisible(isVisible);
        dock.setWidgetSize(pager, isVisible ? 30 : 0);
        pager.setPage(0);
	}

	@Override
	public void setTableElementId(String id) {
		this.getElement().setId(id);
	}
}
