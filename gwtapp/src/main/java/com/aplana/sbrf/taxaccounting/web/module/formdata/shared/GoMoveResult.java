package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.gwtplatform.dispatch.shared.Result;

public class GoMoveResult implements Result {
	private static final long serialVersionUID = -3399228518519012132L;

    private String uuid;
    private boolean isLock;
    private boolean lockTask;
    private String restartMsg;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isLock() {
        return isLock;
    }

    public void setLock(boolean isLock) {
        this.isLock = isLock;
    }

    public boolean isLockTask() {
        return lockTask;
    }

    public void setLockTask(boolean lockTask) {
        this.lockTask = lockTask;
    }

    public String getRestartMsg() {
        return restartMsg;
    }

    public void setRestartMsg(String restartMsg) {
        this.restartMsg = restartMsg;
    }
}