package com.aplana.sbrf.taxaccounting.web.module.configuration.shared;

import com.gwtplatform.dispatch.shared.Result;

public class CheckReadWriteAccessResult implements Result {
    private String uuid;

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }
}
