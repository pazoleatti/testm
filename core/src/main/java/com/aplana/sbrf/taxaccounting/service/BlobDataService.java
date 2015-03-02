package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.BlobData;

import java.io.File;
import java.io.InputStream;
import java.util.List;

/**
 * User: avanteev
 */
public interface BlobDataService {

    /**
     * Создание записи
     * @param is содержимое файла
     * @param name имя для базы
     * @return uuid идентификатор
     */
    String create(InputStream is, String name);

    /**
     * Создание записи
     * @param file файл
     * @param name имя для базы
     * @return uuid идентификатор
     */
    String create(File file, String name);

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
     * Получение данных из фйалового хранилища
     *
     * @param blobId uuid
     * @return {@link BlobData}
     */
    BlobData get(String blobId);

    /**
     * Удаление записей, на которые нет ссылок из других таблиц и которые старше 24 часов
     * @return Количество удаленных запсией
     */
    int clean();
}
