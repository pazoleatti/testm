package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.gwtplatform.dispatch.shared.Result;

public class CreateReportResult implements Result {
    private static final long serialVersionUID = 7832261980997033051L;

    public static enum StatusCreateReport {
        NOT_EXIST_XML, //не существует XML
        EXIST, //существует
        LOCKED, //есть блокировка
        CREATE //создана новая задача
    }

    private StatusCreateReport status;
    private String uuid;

    public StatusCreateReport getStatus() {
        return status;
    }

    public void setStatus(StatusCreateReport status) {
        this.status = status;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
