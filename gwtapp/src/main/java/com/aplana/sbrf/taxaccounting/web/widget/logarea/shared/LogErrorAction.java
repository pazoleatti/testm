package com.aplana.sbrf.taxaccounting.web.widget.logarea.shared;

import com.aplana.sbrf.taxaccounting.model.log.GWTLogEntry;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

/**
 * User: avanteev
 */
public class LogErrorAction extends UnsecuredActionImpl<LogErrorResult> implements ActionName {
    private List<GWTLogEntry> logEntries;

    public List<GWTLogEntry> getLogEntries() {
        return logEntries;
    }

    public void setLogEntries(List<GWTLogEntry> logEntries) {
        this.logEntries = logEntries;
    }

    @Override
    public String getName() {
        return "Печать спеиска ошибок по форме.";
    }
}
