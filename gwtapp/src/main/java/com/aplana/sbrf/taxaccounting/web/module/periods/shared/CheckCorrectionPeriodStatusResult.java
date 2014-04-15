package com.aplana.sbrf.taxaccounting.web.module.periods.shared;

import com.aplana.sbrf.taxaccounting.model.PeriodStatusBeforeOpen;
import com.gwtplatform.dispatch.shared.Result;

public class CheckCorrectionPeriodStatusResult implements Result {
    PeriodStatusBeforeOpen status;

    public PeriodStatusBeforeOpen getStatus() {
        return status;
    }

    public void setStatus(PeriodStatusBeforeOpen status) {
        this.status = status;
    }
}
