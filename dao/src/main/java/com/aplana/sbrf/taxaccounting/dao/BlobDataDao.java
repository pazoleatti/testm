package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.BlobData;

import java.io.InputStream;
import java.util.List;

/**
 * Интерфейс доступа к базе данных для {@link BlobData}.
 */
public interface BlobDataDao {

    /**
     * Создание с текущей датой БД.
     *
     * @param newBlobData {@link BlobData}
     * @return uuid идентификатор созданного объекта
     */
    String createWithSysdate(BlobData newBlobData);

    /**
     * Создание.
     *
     * @param newBlobData {@link BlobData}
     * @return uuid идентификатор созданного объекта
     */
    String create(BlobData newBlobData);

    /**
     * Удаление по указанному идентификатору
     *
     * @param uuid идентификатор
     */
    void delete(String uuid);

    /**
     * Удаление по набору указанных идентикаторов
     *
     * @param uuids набор uuid
     */
    void delete(List<String> uuids);

    /**
     * Обновление поля с данными по идентификатору
     *
     * @param inputStream данные для сохранения
     * @param uuid идентификатор
     */
    void updateDataByUUID(String uuid, InputStream inputStream);

    /**
     * Получение по идентификатору
     *
     * @param uuid идентификатор
     * @return {@link BlobData}
     */
    BlobData fetch(String uuid);

    /**
     * Получение длины данных по идентификатору.
     *
     * @param uuid идентикатор
     * @return длина данных
     */
    long fetchLength(String uuid);

    /**
     * Удаление записей, на которые нет ссылок из других таблиц и которые старше 24 часов
     *
     * @return количество удаленных записей
     */
    long clean();
}