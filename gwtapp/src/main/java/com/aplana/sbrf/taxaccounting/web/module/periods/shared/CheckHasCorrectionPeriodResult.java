package com.aplana.sbrf.taxaccounting.web.module.periods.shared;

import com.gwtplatform.dispatch.shared.Result;

public class CheckHasCorrectionPeriodResult implements Result {
    boolean hasCorrectionPeriods;

    public boolean isHasCorrectionPeriods() {
        return hasCorrectionPeriods;
    }

    public void setHasCorrectionPeriods(boolean hasCorrectionPeriods) {
        this.hasCorrectionPeriods = hasCorrectionPeriods;
    }
}
