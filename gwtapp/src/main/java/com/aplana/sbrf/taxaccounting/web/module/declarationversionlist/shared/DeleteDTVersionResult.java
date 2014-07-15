package com.aplana.sbrf.taxaccounting.web.module.declarationversionlist.shared;

import com.gwtplatform.dispatch.shared.Result;

/**
 * User: avanteev
 */
public class DeleteDTVersionResult implements Result {
    private String logEntryUuid;
    private boolean isLastVersion;

    public boolean isLastVersion() {
        return isLastVersion;
    }

    public void setLastVersion(boolean lastVersion) {
        isLastVersion = lastVersion;
    }

    public String getLogEntryUuid() {
        return logEntryUuid;
    }

    public void setLogEntryUuid(String logEntryUuid) {
        this.logEntryUuid = logEntryUuid;
    }
}
