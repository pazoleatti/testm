package com.aplana.sbrf.taxaccounting.web.module.declarationversionlist.shared;

import com.gwtplatform.dispatch.shared.Result;

/**
 * User: avanteev
 */
public class DeleteDTVersionResult implements Result {
    private String logEntryUuid;

    public String getLogEntryUuid() {
        return logEntryUuid;
    }

    public void setLogEntryUuid(String logEntryUuid) {
        this.logEntryUuid = logEntryUuid;
    }
}
