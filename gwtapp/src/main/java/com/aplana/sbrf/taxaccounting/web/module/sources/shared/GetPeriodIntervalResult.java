package com.aplana.sbrf.taxaccounting.web.module.sources.shared;

import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.PeriodsInterval;
import com.gwtplatform.dispatch.shared.Result;

public class GetPeriodIntervalResult  implements Result {
    private static final long serialVersionUID = 4393163154488905440L;
    private PeriodsInterval periodsInterval;

    public PeriodsInterval getPeriodsInterval() {
        return periodsInterval;
    }

    public void setPeriodsInterval(PeriodsInterval periodsInterval) {
        this.periodsInterval = periodsInterval;
    }
}
