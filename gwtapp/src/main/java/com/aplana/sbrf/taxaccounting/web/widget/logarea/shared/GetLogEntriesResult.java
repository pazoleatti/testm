package com.aplana.sbrf.taxaccounting.web.widget.logarea.shared;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.gwtplatform.dispatch.shared.Result;

import java.util.Map;

public class GetLogEntriesResult implements Result {
    private PagingResult<LogEntry> logEntries;
    private Map<LogLevel, Integer> logEntriesCount;

    public PagingResult<LogEntry> getLogEntries() {
        return logEntries;
    }

    public void setLogEntries(PagingResult<LogEntry> logEntries) {
        this.logEntries = logEntries;
    }

    public Map<LogLevel, Integer> getLogEntriesCount() {
        return logEntriesCount;
    }

    public void setLogEntriesCount(Map<LogLevel, Integer> logEntriesCount) {
        this.logEntriesCount = logEntriesCount;
    }
}
