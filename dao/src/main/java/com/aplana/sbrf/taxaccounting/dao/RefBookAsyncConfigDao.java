package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;

import java.util.List;
import java.util.Map;

/**
 * Дао для работы с настроками асинхронных задач
 * @author dloshkarev
 */
public interface RefBookAsyncConfigDao {
    PagingResult<Map<String,RefBookValue>> getRecords();

    void updateRecords(List<Map<String, RefBookValue>> records);
}
