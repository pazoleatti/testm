package com.aplana.sbrf.taxaccounting.web.module.sources.server;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.GetPeriodIntervalAction;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.GetPeriodIntervalResult;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.CurrentAssign;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.PeriodInfo;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.PeriodsInterval;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class GetPeriodIntervalHandler extends AbstractActionHandler<GetPeriodIntervalAction, GetPeriodIntervalResult> {

    @Autowired
    private RefBookFactory rbFactory;

    private static final Long PERIOD_CODE_REFBOOK = RefBook.Id.PERIOD_CODE.getId();

    public GetPeriodIntervalHandler() {
        super(GetPeriodIntervalAction.class);
    }

    @Override
    public GetPeriodIntervalResult execute(GetPeriodIntervalAction action, ExecutionContext context) throws ActionException {
        GetPeriodIntervalResult result = new GetPeriodIntervalResult();

        /** Получение информации по периодам из справочника Коды, определяющие налоговый (отчётный) период*/
        RefBook refBook = rbFactory.get(PERIOD_CODE_REFBOOK);
        RefBookDataProvider provider = rbFactory.getDataProvider(refBook.getId());
        String filter = action.getTaxType().getCode() + " = 1";
        PagingResult<Map<String, RefBookValue>> records = provider.getRecords(new Date(), null, filter ,null);
        if (records.isEmpty()) {
            throw new ServiceException("Некорректные данные в справочнике \"Коды, определяющие налоговый (отчётный) период\"");
        }

        Map<CurrentAssign, PeriodsInterval> periodsIntervals = new HashMap<CurrentAssign, PeriodsInterval>();
        Date minStartDate = null;
        Date maxEndDate = null;
        for (CurrentAssign currentAssign : action.getCurrentAssigns()) {
            if (minStartDate == null) {
                minStartDate = currentAssign.getStartDateAssign();
                maxEndDate = currentAssign.getEndDateAssign();
            }
            if (currentAssign.getStartDateAssign().before(minStartDate)) {
                minStartDate = currentAssign.getStartDateAssign();
            }
            if (currentAssign.getEndDateAssign() == null || (maxEndDate != null && currentAssign.getEndDateAssign().after(maxEndDate))) {
                maxEndDate = currentAssign.getEndDateAssign();
            }

            PeriodsInterval periodsInterval = fillPeriodInfo(currentAssign.getStartDateAssign(), currentAssign.getEndDateAssign(), records);
            periodsIntervals.put(currentAssign, periodsInterval);
        }

        PeriodsInterval commonInterval = fillPeriodInfo(minStartDate, maxEndDate, records);
        result.setPeriodsIntervals(periodsIntervals);
        result.setPeriodsInterval(commonInterval);
        return result;
    }

    @Override
    public void undo(GetPeriodIntervalAction action, GetPeriodIntervalResult result, ExecutionContext context) throws ActionException {
        //Do nothing
    }

    private PeriodsInterval fillPeriodInfo(Date periodDateFrom, Date periodDateTo, PagingResult<Map<String, RefBookValue>> records) {
        PeriodsInterval interval = new PeriodsInterval();
        Calendar calendarStart = Calendar.getInstance();
        calendarStart.setTime(periodDateFrom);
        int yearFrom = calendarStart.get(Calendar.YEAR);
        calendarStart.set(Calendar.YEAR, 1970);
        Date dateFrom = calendarStart.getTime();

        Date dateTo = null;
        Integer yearTo = null;
        if (periodDateTo != null) {
            Calendar calendarEnd = Calendar.getInstance();
            calendarEnd.setTime(periodDateTo);
            yearTo = calendarEnd.get(Calendar.YEAR);
            calendarEnd.set(Calendar.YEAR, 1970);
            dateTo = calendarEnd.getTime();
        }

        PeriodInfo periodFrom = null;
        PeriodInfo periodTo = null;
        for (Map<String, RefBookValue> record : records) {
            PeriodInfo period = new PeriodInfo();
            period.setName(record.get("NAME").getStringValue());
            period.setCode(record.get("CODE").getStringValue());
            period.setStartDate(record.get("CALENDAR_START_DATE").getDateValue());
            period.setEndDate(record.get("END_DATE").getDateValue());
            if (period.getStartDate().equals(dateFrom)) {
                periodFrom = period;
            }
            if (period.getEndDate().equals(dateTo)) {
                periodTo = period;
            }
        }
        interval.setYearFrom(yearFrom);
        interval.setYearTo(yearTo);
        interval.setPeriodFrom(periodFrom);
        interval.setPeriodTo(periodTo);
        return interval;
    }

}
