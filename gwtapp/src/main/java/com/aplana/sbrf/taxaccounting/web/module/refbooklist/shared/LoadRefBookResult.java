package com.aplana.sbrf.taxaccounting.web.module.refbooklist.shared;

import com.gwtplatform.dispatch.shared.Result;

public class LoadRefBookResult implements Result {
	private static final long serialVersionUID = -8740180359930296291L;
	
	private String uuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
