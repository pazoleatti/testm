package com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared;

import com.gwtplatform.dispatch.shared.Result;

public class CreateManualFormDataResult implements Result {
    private static final long serialVersionUID = -3634232098774018293L;
    private String uuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
