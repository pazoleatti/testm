package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.BlobData;

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
     * @param blobId
     */
    void delete(String blobId);

    /**
     * Обновление записи в базе
     * @param blobId
     * @param is
     */
    void save(String blobId, InputStream is);

    /**
     * ПОлучение данных из фйалового хранилища
     *
     * @param blobId
     * @return
     */
    BlobData get(String blobId);
}
