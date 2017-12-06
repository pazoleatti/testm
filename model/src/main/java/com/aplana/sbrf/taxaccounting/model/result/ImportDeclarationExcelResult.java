package com.aplana.sbrf.taxaccounting.model.result;

import com.aplana.sbrf.taxaccounting.model.CreateAsyncTaskStatus;

public class ImportDeclarationExcelResult extends DeclarationDataResult {
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
