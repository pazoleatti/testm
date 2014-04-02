package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.BlobData;

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
     * @param blobData {@link BlobData}
     */
    void save(BlobData blobData);

    /**
     * Получение записи из таблицы.
     * @param uuid идентификатор
     * @return {@link BlobData}
     */
    BlobData get(String uuid);
}
