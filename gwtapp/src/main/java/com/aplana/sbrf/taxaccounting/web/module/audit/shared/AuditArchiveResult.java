package com.aplana.sbrf.taxaccounting.web.module.audit.shared;

import com.gwtplatform.dispatch.shared.Result;

/**
 * User: avanteev
 */
public class AuditArchiveResult implements Result {
    String uuid;
    private int countOfRemoveRecords;

    public int getCountOfRemoveRecords() {
        return countOfRemoveRecords;
    }

    public void setCountOfRemoveRecords(int countOfRemoveRecords) {
        this.countOfRemoveRecords = countOfRemoveRecords;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
