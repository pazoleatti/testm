package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.gwtplatform.dispatch.shared.Result;

public class CreateReportResult implements Result {
    private static final long serialVersionUID = 7832261980997033051L;

    private boolean existReport = false;
    private boolean lock;
    private String uuid;
    private String restartMsg;

    public boolean isExistReport() {
        return existReport;
    }

    public void setExistReport(boolean existReport) {
        this.existReport = existReport;
    }

    public boolean isLock() {
        return lock;
    }

    public void setLock(boolean lock) {
        this.lock = lock;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getRestartMsg() {
        return restartMsg;
    }

    public void setRestartMsg(String restartMsg) {
        this.restartMsg = restartMsg;
    }
}
