package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;

import java.util.Map;

/**
 * Dao для получение списка кодов налоговоых органов и КПП:
 * _code - уникальные коды из справочника 200 "Параметры представления деклараций по налогу на имущество".
 * _kpp - уникальные КПП из справочника 200 "Параметры представления деклараций по налогу на имущество".
 */
public interface RefBookTaxOrganDao {

    Long REF_BOOK_CODE_ID = 204L;
    Long REF_BOOK_KPP_ID = 205L;

    PagingResult<Map<String, RefBookValue>> getRecords(Long refBookId);

    PagingResult<Map<String, RefBookValue>> getRecords(Long refBookId, String filter);

    Map<String, RefBookValue> getRecordData(Long refBookId, Long recordId);

    int getRecordsCount(Long refBookId, String filter);
}
