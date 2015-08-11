package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.gwtplatform.dispatch.shared.Result;

/**
 * Результат выполнения действий, которые моифицируют форму каким-либо образом.
 *
 * @author Eugene Stetsenko
 */
public class UploadFormDataResult implements Result {
	private static final long serialVersionUID = -4686362790466910194L;

    private String uuid;
    private boolean lock;
    private boolean lockTask;
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