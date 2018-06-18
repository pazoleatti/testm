package com.aplana.sbrf.taxaccounting.service.impl.refbook;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;

import java.util.Map;

/**
 * Класс для обработки групп записей справочника. Используется для тяжелых операций, которые потенциально потребляют много памяти
 * @author dloshkarev
 */
public interface BatchIterator {
    int BATCH_SIZE = 10000;

    /**
     * Есть еще данные?
     */
    boolean hasNext();

    /**
     * Получает следующую запись из батча
     * @return запись справочника
     */
    Map<String,RefBookValue> getNextRecord();
}
