package com.aplana.sbrf.taxaccounting.script.service;

import com.aplana.sbrf.taxaccounting.service.ScriptExposed;

import java.util.List;
import java.util.Set;

@ScriptExposed
public interface PersonService {

    List<Long> getDuplicate(Set<Long> originalRecordId);

    /**
     * Получить количество уникальных записей в справочнике физлиц, которые не являются дублями для налоговой формы.
     * @param declarationDataId идентификатор налоговой формы
     * @return  количество записей
     */
    int getCountOfUniqueEntries(long declarationDataId);

}
