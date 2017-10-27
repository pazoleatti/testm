package com.aplana.sbrf.taxaccounting.script.dao;

import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.service.ScriptExposed;
import org.joda.time.LocalDateTime;

import java.io.File;

/**
 * Интерфейс получения бинарных данных
 */
@ScriptExposed
public interface BlobDataService {

    /**
     * Получение записи из таблицы
     *
     * @param uuid
     * @return
     */
    BlobData get(String uuid);

    /**
     * Создание записи
     *
     * @param file       файл
     * @param name       имя для базы
     * @param createDate дата (например, полученная из xml, как в декларациях)
     * @return uuid идентификатор
     */
    String create(File file, String name, LocalDateTime createDate);
}