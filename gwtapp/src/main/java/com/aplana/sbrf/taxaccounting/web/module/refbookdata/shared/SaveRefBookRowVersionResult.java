package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.gwtplatform.dispatch.shared.Result;

public class SaveRefBookRowVersionResult implements Result {
    private String uuid;

    private boolean exception;

    private boolean checkRegion;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isException() {
        return exception;
    }

    public void setException(boolean exception) {
        this.exception = exception;
    }

    public boolean isCheckRegion() {
        return checkRegion;
    }

    public void setCheckRegion(boolean checkRegion) {
        this.checkRegion = checkRegion;
    }
}
