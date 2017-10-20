package com.aplana.sbrf.taxaccounting.web.widget.logarea.shared;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.log.GWTLogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.gwtplatform.dispatch.shared.Result;

import java.util.Map;

public class GetLogEntriesResult implements Result {
    private PagingResult<GWTLogEntry> logEntries;
    private Map<LogLevel, Integer> logEntriesCount;

    public PagingResult<GWTLogEntry> getLogEntries() {
        return logEntries;
    }

    public void setLogEntries(PagingResult<GWTLogEntry> logEntries) {
        this.logEntries = logEntries;
    }

    public Map<LogLevel, Integer> getLogEntriesCount() {
        return logEntriesCount;
    }

    public void setLogEntriesCount(Map<LogLevel, Integer> logEntriesCount) {
        this.logEntriesCount = logEntriesCount;
    }
}
