package com.aplana.sbrf.taxaccounting.model.result;

import com.aplana.sbrf.taxaccounting.model.CreateAsyncTaskStatus;
import com.aplana.sbrf.taxaccounting.model.result.DeclarationDataResult;

public class CheckDeclarationResult extends DeclarationDataResult {

    private String uuid;
    private CreateAsyncTaskStatus status;
    private String restartMsg;

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

    public void setRestartMsg(String restartMsg) {
        this.restartMsg = restartMsg;
    }

    public String getRestartMsg() {
        return restartMsg;
    }
}
