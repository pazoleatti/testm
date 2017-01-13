package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

/*
* Интерфейс получения бинарных данных
* */
@ScriptExposed
public interface BlobDataService {


    /**
     * Получение записи из таблицы
     * @param uuid
     * @return
     */
    BlobData get(String uuid);
}
