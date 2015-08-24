package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * User: avanteev
 */
public class CheckOpenReportPeriodAction extends UnsecuredActionImpl<CheckOpenReportPeriodResult> {
    private int depId;

    public int getDepId() {
        return depId;
    }

    public void setDepId(int depId) {
        this.depId = depId;
    }
}
