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
    public PagingResult<Map<String, RefBookValue>> getRecords(Long refBookId, Integer reportPeriodId, PagingParams pagingParams, String filter, RefBookAttribute sortAttribute);

    Map<String, RefBookValue> getRecordData(Long refBookId, Long recordId);

    RefBookValue getValue(Long refBookId, Long recordId, Long attributeId);

    void insert(Long refBookId, Integer reportPeriodId, List<Map<String, RefBookValue>> records);

    void deleteAll(Integer reportPeriod);

    List<ReportPeriod> gerReportPeriods();
    public void deleete(List<Long> ids);
    public void update(List<Map<String, RefBookValue>> records);
}
