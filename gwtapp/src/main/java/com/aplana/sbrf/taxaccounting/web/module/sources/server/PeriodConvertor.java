package com.aplana.sbrf.taxaccounting.web.module.sources.server;

import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.PeriodsInterval;

import java.util.Calendar;
import java.util.Date;

public class PeriodConvertor {
    public static Date getDateFrom(PeriodsInterval interval) {
        Calendar periodFrom = Calendar.getInstance();
        periodFrom.setTime(interval.getPeriodFrom().getStartDate());
        periodFrom.set(Calendar.YEAR, interval.getYearFrom());
        return periodFrom.getTime();
    }

    public static Date getDateTo(PeriodsInterval interval) {
        Calendar periodTo = Calendar.getInstance();
        periodTo.setTime(interval.getPeriodTo().getEndDate());
        periodTo.set(Calendar.YEAR, interval.getYearTo());
        return periodTo.getTime();
    }
}
