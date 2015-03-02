package com.aplana.sbrf.taxaccounting.web.module.audit.shared;

import com.gwtplatform.dispatch.shared.Result;

/**
 * User: avanteev
 */
public class PrintAuditDataResult implements Result {
    private String logUuid;

    public String getLogUuid() {
        return logUuid;
    }

    public void setLogUuid(String logUuid) {
        this.logUuid = logUuid;
    }
}
