package com.aplana.sbrf.taxaccounting.web.widget.log;

import com.aplana.sbrf.taxaccounting.model.log.GWTLogEntry;
import com.google.gwt.view.client.AsyncDataProvider;

import java.util.List;

public interface LogEntriesView{
    void setTableElementId(String id);
    void setLogEntries(int start, int totalCount,  List<GWTLogEntry> logEntries);
    void clearLogEntries();
    void setDataProvider(AsyncDataProvider<GWTLogEntry> dataProvider);
    int getPageSize();
}
