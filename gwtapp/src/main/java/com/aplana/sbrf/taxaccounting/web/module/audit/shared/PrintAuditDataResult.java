package com.aplana.sbrf.taxaccounting.web.module.audit.shared;

import com.gwtplatform.dispatch.shared.Result;

/**
 * User: avanteev
 */
public class PrintAuditDataResult implements Result {
    private String logUuid;
    private boolean lock;
    private String uuid;
    private String restartMsg;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isLock() {
        return lock;
    }

    public void setLock(boolean lock) {
        this.lock = lock;
    }

    public String getRestartMsg() {
        return restartMsg;
    }

    public void setRestartMsg(String restartMsg) {
        this.restartMsg = restartMsg;
    }

    public String getLogUuid() {
        return logUuid;
    }

    public void setLogUuid(String logUuid) {
        this.logUuid = logUuid;
    }
}
