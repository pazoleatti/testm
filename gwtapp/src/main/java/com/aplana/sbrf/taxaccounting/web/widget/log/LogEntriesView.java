package com.aplana.sbrf.taxaccounting.web.widget.log;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.google.gwt.view.client.AsyncDataProvider;

import java.util.List;

public interface LogEntriesView{
    public void setTableElementId(String id);
    public void setLogEntries(int start, int totalCount,  List<LogEntry> logEntries);
    public void clearLogEntries();
    public void setDataProvider(AsyncDataProvider<LogEntry> dataProvider);
}
