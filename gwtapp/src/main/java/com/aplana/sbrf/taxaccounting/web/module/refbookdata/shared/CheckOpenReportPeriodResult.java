package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.gwtplatform.dispatch.shared.Result;

/**
 * User: avanteev
 */
public class CheckOpenReportPeriodResult implements Result {
    private boolean isHaveOpenPeriod;
    private String uuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isHaveOpenPeriod() {
        return isHaveOpenPeriod;
    }

    public void setHaveOpenPeriod(boolean isHaveOpenPeriod) {
        this.isHaveOpenPeriod = isHaveOpenPeriod;
    }
}
