package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

public class CreateReportResult extends DeclarationDataResult {
    private static final long serialVersionUID = 7832261980997033051L;

    private CreateAsyncTaskStatus status;
    private String uuid;
    private String restartMsg;

    public CreateAsyncTaskStatus getStatus() {
        return status;
    }

    public void setStatus(CreateAsyncTaskStatus status) {
        this.status = status;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setRestartMsg(String restartMsg) {
        this.restartMsg = restartMsg;
    }

    public String getRestartMsg() {
        return restartMsg;
    }
}
