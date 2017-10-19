package com.aplana.sbrf.taxaccounting.web.widget.logarea.shared;

import com.aplana.sbrf.taxaccounting.model.log.GWTLogEntry;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

public class SaveLogEntriesAction extends UnsecuredActionImpl<SaveLogEntriesResult> {
    private List<GWTLogEntry> logEntries;

    public List<GWTLogEntry> getLogEntries() {
        return logEntries;
    }

    public void setLogEntries(List<GWTLogEntry> logEntries) {
        this.logEntries = logEntries;
    }
}
