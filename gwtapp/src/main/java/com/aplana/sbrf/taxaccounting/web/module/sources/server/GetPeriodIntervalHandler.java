package com.aplana.sbrf.taxaccounting.web.module.sources.server;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.GetPeriodIntervalAction;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.GetPeriodIntervalResult;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.PeriodInfo;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.PeriodsInterval;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class GetPeriodIntervalHandler extends AbstractActionHandler<GetPeriodIntervalAction, GetPeriodIntervalResult> {

    @Autowired
    private RefBookFactory rbFactory;

    private static final Long PERIOD_CODE_REFBOOK = 8L;

    public GetPeriodIntervalHandler() {
        super(GetPeriodIntervalAction.class);
    }

    @Override
    public GetPeriodIntervalResult execute(GetPeriodIntervalAction action, ExecutionContext context) throws ActionException {
        GetPeriodIntervalResult result = new GetPeriodIntervalResult();

        Calendar calendarStart = Calendar.getInstance();
        calendarStart.setTime(action.getPeriodStart());
        int year = calendarStart.get(Calendar.YEAR);
        calendarStart.set(Calendar.YEAR, 1970);
        Date dateFrom = calendarStart.getTime();

        Date dateTo = null;
        if (action.getPeriodEnd() != null) {
            Calendar calendarEnd = Calendar.getInstance();
            calendarEnd.setTime(action.getPeriodEnd());
            calendarEnd.set(Calendar.YEAR, 1970);
            dateTo = calendarEnd.getTime();
        }

        /** Получение информации по периодам из справочника Коды, определяющие налоговый (отчётный) период*/
        RefBook refBook = rbFactory.get(PERIOD_CODE_REFBOOK);
        RefBookDataProvider provider = rbFactory.getDataProvider(refBook.getId());

        String filter = action.getTaxType().getCode() + " = 1";
        PagingResult<Map<String, RefBookValue>> records = provider.getRecords(new Date(), null, filter ,null);
        if (records.isEmpty()) {
            throw new ServiceException("Некорректные данные в справочнике \"Коды, определяющие налоговый (отчётный) период\"");
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
            if (dateTo != null && period.getEndDate().equals(dateTo)) {
                periodTo = period;
            }
        }

        PeriodsInterval periodsInterval = new PeriodsInterval();
        periodsInterval.setYearFrom(year);
        periodsInterval.setYearTo(year);
        periodsInterval.setPeriodFrom(periodFrom);
        periodsInterval.setPeriodTo(periodTo);
        result.setPeriodsInterval(periodsInterval);
        return result;
    }

    @Override
    public void undo(GetPeriodIntervalAction action, GetPeriodIntervalResult result, ExecutionContext context) throws ActionException {
        //Do nothing
    }
}
