package com.aplana.sbrf.taxaccounting.web.module.bookerstatements.shared;

import com.gwtplatform.dispatch.shared.Result;

public class CreateBookerStatementsResult implements Result {
    private static final long serialVersionUID = 24153128453321804L;

    private String uuid;
    private boolean hasError = false;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isHasError() {
        return hasError;
    }

    public void setHasError(boolean hasError) {
        this.hasError = hasError;
    }
}
