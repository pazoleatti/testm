package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

/**
 * User: avanteev
 */
public class GetFileUploadResult implements Result {

    private List<LogEntry> logEntries;

    public List<LogEntry> getLogEntries() {
        return logEntries;
    }

    public void setLogEntries(List<LogEntry> logEntries) {
        this.logEntries = logEntries;
    }
}
