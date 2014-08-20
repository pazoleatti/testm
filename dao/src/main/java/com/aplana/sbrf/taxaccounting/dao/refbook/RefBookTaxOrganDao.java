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

    static final Long REF_BOOK_CODE_ID = 204L;
    static final Long REF_BOOK_KPP_ID = 205L;

    PagingResult<Map<String, RefBookValue>> getRecordsCode();

    PagingResult<Map<String, RefBookValue>> getRecordsCode(String filter);

    PagingResult<Map<String, RefBookValue>> getRecordsKpp();

    PagingResult<Map<String, RefBookValue>> getRecordsKpp(String filter);

}
