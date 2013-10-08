package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;

import java.util.List;
import java.util.Map;

/**
 * DAO для работы справочника "Отчет о прибылях и убытках (Форма 0409102-СБ)"
 * @author Dmitriy Levykin
 */
public interface RefBookIncome102Dao {

	public static final Long REF_BOOK_ID = 52L;

    /**
     * Получение записей справочника
     *
     * @param reportPeriodId
     * @param pagingParams
     * @param filter
     * @param sortAttribute
     * @return
     */
    PagingResult<Map<String, RefBookValue>> getRecords(Integer reportPeriodId, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute);

    /**
     * Получение записи справочника по recordId
     * @param recordId
     * @return
     */
    Map<String, RefBookValue> getRecordData(Long recordId);

    /**
     * Перечень имеющихся отчетных периодов среди записей
     * @return
     */
    List<ReportPeriod> gerReportPeriods();

    /**
     * Обновление записей справочника
     * @param records
     */
    void updateRecords(List<Map<String, RefBookValue>> records);
}
