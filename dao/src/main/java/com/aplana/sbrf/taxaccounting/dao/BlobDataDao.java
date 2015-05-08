package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.BlobData;

import java.io.InputStream;
import java.util.List;

/**
 * User: avanteev
 */
public interface BlobDataDao {

    /**
     * Создание записи в таблице.
     * @param blobData {@link BlobData}
     * @return uuid идентификатор
     */
    String create(BlobData blobData);

    /**
     * Создание записи в таблице.
     * Проставляет свою дату, а не дату бд.
     * @param blobData {@link BlobData}
     * @return uuid идентификатор
     */
    String createWithDate(BlobData blobData);

    /**
     * Удаление записи
     * @param uuid идентификатор
     */
    void delete(String uuid);

    /**
     * Удаление записей
     * @param uuidStrings набор uuid для удаления
     */
    void delete(List<String> uuidStrings);

    /**
     * Обновление уже существующей записи.
     * Обновляются только само поле с данными файла.
     * @param dataIn данные для сохранения
     * @param uuid id записи для обновления
     */
    void save(String uuid, InputStream dataIn);

    /**
     * Получение записи из таблицы.
     * @param uuid идентификатор
     * @return {@link BlobData}
     */
    BlobData get(String uuid);

    /**
     * Получение длины данных в blob
     * @param uuid
     * @return
     */
    long getLength(String uuid);

    /**
     * Удаление записей, на которые нет ссылок из других таблиц и которые старше 24 часов
     * @return Количество удаленных запсией
     */
    int clean();
}
