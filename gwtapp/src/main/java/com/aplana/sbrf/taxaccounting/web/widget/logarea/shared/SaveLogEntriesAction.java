package com.aplana.sbrf.taxaccounting.web.widget.logarea.shared;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

public class SaveLogEntriesAction extends UnsecuredActionImpl<SaveLogEntriesResult> {
    private List<LogEntry> logEntries;

    public List<LogEntry> getLogEntries() {
        return logEntries;
    }

    public void setLogEntries(List<LogEntry> logEntries) {
        this.logEntries = logEntries;
    }
}
