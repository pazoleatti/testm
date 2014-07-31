package com.aplana.sbrf.taxaccounting.web.module.audit.shared;

import com.gwtplatform.dispatch.shared.Result;

/**
 * User: avanteev
 */
public class AuditArchiveResult implements Result {
    String uuid;
    String fileUuid;
    private int countOfRemoveRecords;
    private boolean exception = false;

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

    public String getFileUuid() {
        return fileUuid;
    }

    public void setFileUuid(String fileUuid) {
        this.fileUuid = fileUuid;
    }

    public boolean isException() {
        return exception;
    }

    public void setException(boolean exception) {
        this.exception = exception;
    }
}
