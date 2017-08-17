package com.aplana.sbrf.taxaccounting.model;

import org.joda.time.LocalDateTime;

import java.io.InputStream;

/**
 * Модель для работы с файловым хранилищем.
 */
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
    private LocalDateTime creationDate;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }
}