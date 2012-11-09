package com.aplana.sbrf.taxaccounting.gwtapp.shared;

import com.aplana.sbrf.taxaccounting.log.LogEntry;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

/** @author Vitalii Samolovskikh */
public class SaveDataResult implements Result {
    private List<LogEntry> logEntries;

    public SaveDataResult() {
    }

    public List<LogEntry> getLogEntries() {
        return logEntries;
    }

    public void setLogEntries(List<LogEntry> logEntries) {
        this.logEntries = logEntries;
    }
}
