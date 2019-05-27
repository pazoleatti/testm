package com.aplana.sbrf.taxaccounting.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.InputStream;
import java.util.Date;

/**
 * Модель для работы с файловым хранилищем.
 */
@Getter
@Setter
@NoArgsConstructor
public class BlobData {
    /**
     * Уникальный иденификатор записи.
     * Сам класс его не генерирует.
     */
    private String uuid;
    /**
     * Наименование файла вместе с расширением
     */
    private String name;
    /**
     * Данные в виде {@link InputStream}
     */
    private InputStream inputStream;
    /**
     * Дата создания
     */
    private Date creationDate;

    public BlobData(String uuid) {
        this.uuid = uuid;
    }
}