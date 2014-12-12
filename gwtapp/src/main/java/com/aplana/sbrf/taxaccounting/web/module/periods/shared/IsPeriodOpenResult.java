package com.aplana.sbrf.taxaccounting.web.module.periods.shared;

import com.gwtplatform.dispatch.shared.Result;

public class IsPeriodOpenResult implements Result {
    boolean isPeriodOpen;

    public boolean isPeriodOpen() {
        return isPeriodOpen;
    }

    public void setPeriodOpen(boolean isPeriodOpen) {
        this.isPeriodOpen = isPeriodOpen;
    }
}
