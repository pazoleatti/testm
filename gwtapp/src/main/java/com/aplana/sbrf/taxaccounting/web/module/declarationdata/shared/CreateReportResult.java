package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.gwtplatform.dispatch.shared.Result;

public class CreateReportResult implements Result {
    private static final long serialVersionUID = 7832261980997033051L;

    private CreateAsyncTaskStatus status;
    private String uuid;

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
}
