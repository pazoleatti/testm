package com.aplana.sbrf.taxaccounting.web.module.audit.shared;

import com.gwtplatform.dispatch.shared.Result;

/**
 * User: avanteev
 */
public class PrintAuditDataResult implements Result {
    private String uuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
