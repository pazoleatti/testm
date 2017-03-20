package com.aplana.sbrf.taxaccounting.web.widget.log;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.web.widget.log.cell.LogEntryDateCell;
import com.aplana.sbrf.taxaccounting.web.widget.log.cell.LogEntryImageCell;
import com.aplana.sbrf.taxaccounting.web.widget.log.cell.LogEntryIndexCell;
import com.aplana.sbrf.taxaccounting.web.widget.log.cell.LogEntryTextCell;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericCellTable;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
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
    GenericCellTable<LogEntry> logCellTable;

    @UiField
    FlexiblePager pager;

    @UiField
    DockLayoutPanel dock;

    private AsyncDataProvider<LogEntry> dataProvider;

	public LogEntriesWidget() {
		initWidget(uiBinder.createAndBindUi(this));

		Column<LogEntry, LogEntry> messageColumn = new Column<LogEntry, LogEntry>(
				new LogEntryTextCell(){
                    @Override
                    public String getStringValue(LogEntry value) {
                        return value.getMessage();
                    }
                }) {

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

        Column<LogEntry, LogEntry> dateColumn = new Column<LogEntry, LogEntry>(
                new LogEntryDateCell()) {
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

        Column<LogEntry, LogEntry> typeColumn = new Column<LogEntry, LogEntry>(
                new LogEntryTextCell(){
                    @Override
                    public String getStringValue(LogEntry value) {
                        return value.getType()!=null?value.getType():"";
                    }
                }) {
            @Override
            public LogEntry getValue(LogEntry object) {
                return object;
            }
        };

        Column<LogEntry, LogEntry> objectColumn = new Column<LogEntry, LogEntry>(
                new LogEntryTextCell(){
                    @Override
                    public String getStringValue(LogEntry value) {
                        return value.getObject()!=null?value.getObject():"";
                    }
                }) {
            @Override
            public LogEntry getValue(LogEntry object) {
                return object;
            }
        };

		logCellTable.addColumn(indexColumn);
		logCellTable.setColumnWidth(indexColumn, "30px");
        logCellTable.addColumn(dateColumn);
        logCellTable.setColumnWidth(dateColumn, "110px");
		logCellTable.addColumn(imageColumn);
		logCellTable.addColumn(messageColumn);
		logCellTable.addColumn(typeColumn);
        logCellTable.setColumnWidth(typeColumn, "100px");
        logCellTable.addColumn(objectColumn);
        logCellTable.setColumnWidth(objectColumn, "100px");
	}

    @Override
    protected void onLoad() {
        super.onLoad();
        if (dataProvider == null) {
            return;
        }
        dataProvider.addDataDisplay(logCellTable);
        pager.setDisplay(logCellTable);
        logCellTable.setPageSize(pager.getPageSize());
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

        boolean isVisible = totalCount > pager.getPageSize();
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

    @Override
    public int getPageSize() {
        return pager.getPageSize();
    }
}
