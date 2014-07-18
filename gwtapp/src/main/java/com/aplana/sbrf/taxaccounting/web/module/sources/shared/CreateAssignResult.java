package com.aplana.sbrf.taxaccounting.web.module.sources.shared;

import com.gwtplatform.dispatch.shared.Result;

public class CreateAssignResult implements Result {
    private static final long serialVersionUID = -3215381616911179187L;

    private String uuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
