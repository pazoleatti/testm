package com.aplana.sbrf.taxaccounting.web.widget.log;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.web.widget.log.cell.LogEntryImageCell;
import com.aplana.sbrf.taxaccounting.web.widget.log.cell.LogEntryIndexCell;
import com.aplana.sbrf.taxaccounting.web.widget.log.cell.LogEntryMessageCell;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;

import java.util.List;

public class LogEntriesWidget extends Composite implements LogEntriesView {

	private static LogEntryWidgetUiBinder uiBinder = GWT.create(LogEntryWidgetUiBinder.class);

	interface LogEntryWidgetUiBinder extends UiBinder<Widget, LogEntriesWidget> {
	}

	@UiField
    CellTable<LogEntry> logCellTable;

    @UiField
    FlexiblePager pager;

    @UiField
    DockLayoutPanel dock;

    public static final int PAGE_SIZE = 50;

    private AsyncDataProvider<LogEntry> dataProvider;

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
	}

    @Override
    protected void onLoad() {
        super.onLoad();
        if (dataProvider == null) {
            return;
        }
        dataProvider.addDataDisplay(logCellTable);
        pager.setDisplay(logCellTable);
        logCellTable.setPageSize(PAGE_SIZE);
    }

    @Override
    public void clearLogEntries() {
        logCellTable.setRowCount(0);
        pager.setPage(0);
        pager.setVisible(false);
    }

    @Override
    public void setLogEntries(int start, int totalCount, List<LogEntry> logEntries) {
        if (logEntries == null || logEntries.isEmpty()) {
            clearLogEntries();
            return;
        }

        logCellTable.setRowCount(totalCount);
        logCellTable.setRowData(start, logEntries);

        boolean isVisible = totalCount > PAGE_SIZE;
        pager.setVisible(isVisible);
        dock.setWidgetSize(pager, isVisible ? 30 : 0);
    }

    @Override
    public void setDataProvider(AsyncDataProvider<LogEntry> dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
	public void setTableElementId(String id) {
		this.getElement().setId(id);
	}
}
