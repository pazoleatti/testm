package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;

import java.util.List;
import java.util.Map;

/**
 * Дао для работы с настроками email
 * @author dloshkarev
 */
public interface RefBookEmailConfigDao {
    Long REF_BOOK_EMAIL_CONFIG_ID = 400L;

    PagingResult<Map<String,RefBookValue>> getRecords();

    void updateRecords(List<Map<String, RefBookValue>> records);
}
