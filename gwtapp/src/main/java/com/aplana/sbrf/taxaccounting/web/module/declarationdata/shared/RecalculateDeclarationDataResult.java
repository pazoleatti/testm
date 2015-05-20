package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.gwtplatform.dispatch.shared.Result;

public class RecalculateDeclarationDataResult implements Result {

    public static enum StatusRecalculateDeclaration {
        LOCKED, //есть блокировка
        CREATE //создана новая задача
    }

    private String uuid;
    private StatusRecalculateDeclaration status;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public StatusRecalculateDeclaration getStatus() {
        return status;
    }

    public void setStatus(StatusRecalculateDeclaration status) {
        this.status = status;
    }
}
