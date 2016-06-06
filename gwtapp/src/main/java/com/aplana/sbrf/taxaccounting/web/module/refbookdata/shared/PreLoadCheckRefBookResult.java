package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.gwtplatform.dispatch.shared.Result;

public class PreLoadCheckRefBookResult implements Result {
	private static final long serialVersionUID = 5654117352648218914L;

    private String uuid;
    private boolean isError;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isError() {
        return isError;
    }

    public void setError(boolean isError) {
        this.isError = isError;
    }
}
