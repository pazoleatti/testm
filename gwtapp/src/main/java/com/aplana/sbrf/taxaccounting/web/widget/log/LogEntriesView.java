package com.aplana.sbrf.taxaccounting.web.widget.log;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.google.gwt.view.client.AsyncDataProvider;

import java.util.List;

public interface LogEntriesView{
    void setTableElementId(String id);
    void setLogEntries(int start, int totalCount,  List<LogEntry> logEntries);
    void clearLogEntries();
    void setDataProvider(AsyncDataProvider<LogEntry> dataProvider);
    int getPageSize();
}
