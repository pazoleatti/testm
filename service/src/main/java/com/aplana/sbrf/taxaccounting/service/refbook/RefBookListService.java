package com.aplana.sbrf.taxaccounting.service.refbook;

import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.result.RefBookListResult;

/**
 * Сервис для работы со справочниками
 */
public interface RefBookListService {
    /**
     * Получить все справочники
     * @return список объектов содержащих данные о справочниках
     */
    PagingResult<RefBookListResult> fetchAllRefbooks();
}
