package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.gwtplatform.dispatch.shared.Result;

public class CheckDeclarationDataResult implements Result {

    public static enum StatusCheckDeclaration {
        NOT_EXIST_XML, //не существует XML
        LOCKED, //есть блокировка
        CREATE //создана новая задача
    }

    private String uuid;
    private StatusCheckDeclaration status;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public StatusCheckDeclaration getStatus() {
        return status;
    }

    public void setStatus(StatusCheckDeclaration status) {
        this.status = status;
    }
}
