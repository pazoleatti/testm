package com.aplana.sbrf.taxaccounting.web.module.sources.shared;

import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.CurrentAssign;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.PeriodsInterval;
import com.gwtplatform.dispatch.shared.Result;

import java.util.Map;

public class GetPeriodIntervalResult  implements Result {
    private static final long serialVersionUID = 4393163154488905440L;
    private PeriodsInterval periodsInterval;
    private Map<CurrentAssign, PeriodsInterval> periodsIntervals;

    public Map<CurrentAssign, PeriodsInterval> getPeriodsIntervals() {
        return periodsIntervals;
    }

    public PeriodsInterval getPeriodsInterval() {
        return periodsInterval;
    }

    public void setPeriodsInterval(PeriodsInterval periodsInterval) {
        this.periodsInterval = periodsInterval;
    }

    public void setPeriodsIntervals(Map<CurrentAssign, PeriodsInterval> periodsIntervals) {
        this.periodsIntervals = periodsIntervals;
    }
}
