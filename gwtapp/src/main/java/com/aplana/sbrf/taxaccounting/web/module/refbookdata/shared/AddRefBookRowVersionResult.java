package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class AddRefBookRowVersionResult implements Result {
    private String uuid;

    private List<Long> newIds;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public List<Long> getNewIds() {
        return newIds;
    }

    public void setNewIds(List<Long> newIds) {
        this.newIds = newIds;
    }
}
