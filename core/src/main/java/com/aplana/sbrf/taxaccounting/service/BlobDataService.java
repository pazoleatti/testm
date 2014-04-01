package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.BlobData;

import java.io.InputStream;
import java.util.List;

/**
 * User: avanteev
 */
public interface BlobDataService {

    /**
     * Создание постоянной записи
     * @param is содержимое файла
     * @param name имя для базы
     * @return uuid идентификатор
     */
    String create(InputStream is, String name);

    /**
     * Созадние временной записи в таблице.
     * Предполагается, что шедулер будет очищать таблицу от временных записей.
     * @param is содержимое файла
     * @param name имя для базы
     * @return uuid идентификатор
     */
    String createTemporary(InputStream is, String name);

    /**
     * Удаление записи.
     * @param blobId uuid
     */
    void delete(String blobId);

    /**
     * Удаление записи.
     * @param blobIdStrings набор uuid
     */
    void delete(List<String> blobIdStrings);

    /**
     * Обновление записи в базе
     * @param blobId uuid
     * @param is данные
     */
    void save(String blobId, InputStream is);

    /**
     * ПОлучение данных из фйалового хранилища
     *
     * @param blobId uuid
     * @return {@link BlobData}
     */
    BlobData get(String blobId);
}
