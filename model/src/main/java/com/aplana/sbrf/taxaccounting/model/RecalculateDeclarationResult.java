package com.aplana.sbrf.taxaccounting.model;

public class RecalculateDeclarationResult extends DeclarationDataResult {

    private String uuid;
    private CreateAsyncTaskStatus status;
    private String restartMsg;

    public String getRestartMsg() {
        return restartMsg;
    }

    public void setRestartMsg(String restartMsg) {
        this.restartMsg = restartMsg;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public CreateAsyncTaskStatus getStatus() {
        return status;
    }

    public void setStatus(CreateAsyncTaskStatus status) {
        this.status = status;
    }
}
