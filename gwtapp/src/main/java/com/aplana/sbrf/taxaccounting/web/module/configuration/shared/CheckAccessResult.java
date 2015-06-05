package com.aplana.sbrf.taxaccounting.web.module.configuration.shared;

import com.gwtplatform.dispatch.shared.Result;

public class CheckAccessResult implements Result {
    private String uuid;
    private boolean hasError;

    public boolean isHasError() {
        return hasError;
    }

    public void setHasError(boolean hasError) {
        this.hasError = hasError;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }
}
