package com.aplana.sbrf.taxaccounting.dao.script;

import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

import java.io.File;
import java.util.Date;

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

    /**
     * Создание записи
     * @param file файл
     * @param name имя для базы
     * @param createDate дата (например, полученная из xml, как в декларациях)
     * @return uuid идентификатор
     */
    String create(File file, String name, Date createDate);
}
