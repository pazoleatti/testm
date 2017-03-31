package com.aplana.sbrf.taxaccounting.dao.refbook;

import java.util.List;

public interface NdflReferenceDao {

    /**
     * Обновить значение поля у записей
     * @param uniqueRecordIds
     * @param alias
     * @param value
     */
    int updateField(List<Long> uniqueRecordIds, String alias, String value);
}
