package com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared;

import com.gwtplatform.dispatch.shared.Result;

public class AcceptDeclarationListResult implements Result {
    private String uuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
