package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.gwtplatform.dispatch.shared.Result;

public class FormDataEditResult implements Result {
    private static final long serialVersionUID = 7832261980997033051L;

    private String uuid;
    private boolean lock;
    private String lockMsg;

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

    public String getLockMsg() {
        return lockMsg;
    }

    public void setLockMsg(String lockMsg) {
        this.lockMsg = lockMsg;
    }
}
