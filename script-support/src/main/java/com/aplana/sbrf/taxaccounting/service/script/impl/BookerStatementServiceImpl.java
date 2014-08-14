package com.aplana.sbrf.taxaccounting.service.script.impl;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.script.BookerStatementService;
import com.aplana.sbrf.taxaccounting.service.shared.ScriptComponentContext;
import com.aplana.sbrf.taxaccounting.service.shared.ScriptComponentContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

@Service("bookerStatementService")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class BookerStatementServiceImpl implements BookerStatementService, ScriptComponentContextHolder {

    private ScriptComponentContext context;

    @Autowired
    private RefBookFactory refBookFactory;

    @Override
    public Long getAccountPeriodId(Long departmentId, Date date) {
        if (departmentId == null || date == null) {
            return null;
        }
        // определить код периода
        Map<String, RefBookValue> periodValue = getPeriodValue(date);
        if (periodValue == null) {
            return null;
        }
        Long periodId = periodValue.get(RefBook.RECORD_ID_ALIAS).getNumberValue().longValue();

        // подготовить фильтр для поиска идентификатора периода и подразделения БО из справочника "Периоды и подразделения БО"
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        StringBuilder filter = new StringBuilder();
        filter.append("YEAR = ").append(year);
        filter.append(" and ACCOUNT_PERIOD_ID = ").append(periodId);
        filter.append(" and DEPARTMENT_ID = ").append(departmentId);
        RefBookDataProvider provider = refBookFactory.getDataProvider(107L);
        PagingResult<Map<String, RefBookValue>> records = provider.getRecords(date, null, filter.toString(), null);
        if (records != null && records.size() == 1) {
            RefBookValue rbValue = records.get(0).get(RefBook.RECORD_ID_ALIAS);
            return rbValue.getNumberValue().longValue();
        }
        return null;
    }

    @Override
    public PagingResult<Map<String, RefBookValue>> getRecords(Long refBookId, Long departmentId, Date date, String filter) {
        // проверка id правочника
        if (BookerStatementService.INCOME_101.equals(refBookId) ||
                BookerStatementService.INCOME_102.equals(refBookId)) {
            Long accountPeriodId = getAccountPeriodId(departmentId, date);
            if (accountPeriodId == null) {
                return null;
            }
            String filter1 = "ACCOUNT_PERIOD_ID = " + accountPeriodId;
            if (filter == null || "".equals(filter.trim())) {
                filter = filter1;
            } else {
                filter = filter + " and " + filter1;
            }
            return refBookFactory.getDataProvider(refBookId).getRecords(date, null, filter, null);
        }
        return null;
    }

    @Override
    public Map<String, RefBookValue> getPeriodValue(Date date) {
        Long code;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        if (month <= Calendar.MARCH && dayOfMonth <= 31) { // первый квартал - до 31 марта
            code = 21L;
        } else if (month <= Calendar.JUNE && dayOfMonth <= 30) { // полугодие - до 30 июня
            code = 31L;
        } else if (month <= Calendar.SEPTEMBER && dayOfMonth <= 30) { // 9 месяцев - до 30 сентября
            code = 33L;
        } else { // год - до 31 декабря
            code = 34L;
        }
        RefBookDataProvider provider = refBookFactory.getDataProvider(106L);
        StringBuilder filter = new StringBuilder();
        filter.append("CODE = '").append(code).append("'");
        PagingResult<Map<String, RefBookValue>> records = provider.getRecords(date, null, filter.toString(), null);
        if (records != null && records.size() == 1) {
            return records.get(0);
        }
        return null;
    }

    @Override
    public void setScriptComponentContext(ScriptComponentContext context) {
        this.context = context;
    }
}
