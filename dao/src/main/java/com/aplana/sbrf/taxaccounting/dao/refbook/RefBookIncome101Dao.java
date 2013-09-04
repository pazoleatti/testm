package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;

import java.util.List;
import java.util.Map;

/**
 * DAO для работы справочника оборотной ведомости
 * User: ekuvshinov
 */
public interface RefBookIncome101Dao {
    /**
     * Получение записей справочника
     *
     *
     * @param reportPeriodId
     * @param pagingParams
     * @param filter
     * @param sortAttribute
     * @return
     */
    public PagingResult<Map<String, RefBookValue>> getRecords(Integer reportPeriodId, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute);

    /**
     * Получение записи справочника по recordId
     * @param recordId
     * @return
     */
    public Map<String, RefBookValue> getRecordData(Long recordId);

    /**
     * Перечень имеющихся отчетных периодов среди записей
     * @return
     */
    public List<ReportPeriod> gerReportPeriods();

    /**
     * Обновление записей справочника
     * @param records
     */
    public void updateRecords(List<Map<String, RefBookValue>> records);
}
