package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

import java.util.Date;
import java.util.Map;

/**
 * Сервис для работы с данными бухгалтерской отчетности из скриптов.
 */
@ScriptExposed
public interface BookerStatementService {
    public final static Long INCOME_101 = 50L;
    public final static Long INCOME_102 = 52L;

    /**
     * Получить идентификатор периода и подразделения БО (ACCOUNT_PERIOD_ID) по id подразделения и по дате.
     *
     * @param departmentId идентификатор подразделения
     * @param date дата на которую получаем данные
     * @return идетификатор записи справочника
     */
    Long getAccountPeriodId(Long departmentId, Date date);

    /**
     * Получить данные Бухгалтерской отчетности.
     *
     * @param refBookId отчет 102 или отчет 101
     * @param departmentId идентификатор подразделения
     * @param date дата на которую получаем данные
     * @param filter фильтр
     * @return список данных
     */
    PagingResult<Map<String, RefBookValue>> getRecords(Long refBookId, Long departmentId, Date date, String filter);

    public Map<String, RefBookValue> getPeriodValue(Date date);
}
