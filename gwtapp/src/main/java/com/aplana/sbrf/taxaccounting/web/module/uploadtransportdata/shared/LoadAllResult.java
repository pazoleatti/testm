package com.aplana.sbrf.taxaccounting.web.module.uploadtransportdata.shared;

import com.gwtplatform.dispatch.shared.Result;

public class LoadAllResult implements Result {
    String uuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
