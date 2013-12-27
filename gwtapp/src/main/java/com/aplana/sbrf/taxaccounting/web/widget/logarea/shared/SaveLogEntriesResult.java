package com.aplana.sbrf.taxaccounting.web.widget.logarea.shared;

import com.gwtplatform.dispatch.shared.Result;

public class SaveLogEntriesResult implements Result {

    private String uuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
