package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.BlobData;

/**
 * User: avanteev
 */
public interface BlobDataDao {

    /**
     * Создание записи в таблице.
     * @param blobData
     * @return
     */
    String create(BlobData blobData);

    /**
     * Удаление записи
     * @param uuid
     */
    void delete(String uuid);

    /**
     * Обновление уже существующей записи.
     * Обновляются только само поле с данными файла.
     * @param blobData
     */
    void save(BlobData blobData);

    /**
     * Получение записи из таблицы.
     * @param uuid
     * @return
     */
    BlobData get(String uuid);
}
