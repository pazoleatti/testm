package com.aplana.sbrf.taxaccounting.web.module.periods.shared;

import com.gwtplatform.dispatch.shared.Result;

public class ClosePeriodResult implements Result {
	private String uuid;
    private boolean errorBeforeClose = false;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isErrorBeforeClose() {
        return errorBeforeClose;
    }

    public void setErrorBeforeClose(boolean errorBeforeClose) {
        this.errorBeforeClose = errorBeforeClose;
    }
}
