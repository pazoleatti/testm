package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.gwtplatform.dispatch.shared.Result;

public class CheckDeclarationDataResult implements Result {
    private String uuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
