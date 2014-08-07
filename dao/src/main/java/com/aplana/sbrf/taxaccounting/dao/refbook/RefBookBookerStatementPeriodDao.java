package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;

import java.util.Map;

/**
 * Dao для получение списка периода БО:
 * уникальные год и период (без учета подразделения) из справочника 107 "Периоды и подразделения БО".
 */
public interface RefBookBookerStatementPeriodDao {

    static final Long REF_BOOK_ID = 108L;

    PagingResult<Map<String, RefBookValue>> getRecords();

}
