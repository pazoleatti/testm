package com.aplana.sbrf.taxaccounting.web.module.sources.shared;

import com.gwtplatform.dispatch.shared.Result;

public class UpdateCurrentAssignsResult implements Result {
	private static final long serialVersionUID = -3885276386375273720L;

    private String uuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
