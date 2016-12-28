package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvFile;

/**
 * Сервис для работы с "Файл обмена"
 */
public interface RaschsvFileService {

    /**
     * Сохраняет запись
     * @param raschsvFile
     * @return
     */
    Integer insert(RaschsvFile raschsvFile);
}
