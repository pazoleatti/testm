package com.aplana.sbrf.taxaccounting.service;

import java.io.InputStream;

/**
 * User: avanteev
 */
public interface BlobDataService {

    /**
     * Создание постоянной записи
     * @param is
     * @param name
     * @return
     */
    String create(InputStream is, String name);

    /**
     * Созадние временной записи в таблице.
     * Предполагается, что шедулер будет очищать таблицу от временных записей.
     * @param is
     * @param name
     * @return
     */
    String createTemporary(InputStream is, String name);

    /**
     * Удаление записи.
     * @param blob_id
     */
    void delete(String blob_id);

    /**
     * Обновление записи в базе
     * @param blob_id
     * @param is
     */
    void save(String blob_id, InputStream is);

    /**
     * ПОлучение данных из фйалового хранилища
     * @param blob_id
     * @return
     */
    InputStream get(String blob_id);
}
