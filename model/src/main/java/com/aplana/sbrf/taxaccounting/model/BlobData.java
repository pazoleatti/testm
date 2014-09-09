package com.aplana.sbrf.taxaccounting.model;

import java.io.InputStream;
import java.util.Date;

/**
 * User: avanteev
 * Модель для работы с файловым хранилищем.
 */
public class BlobData {

    /*Уникальный иденификатор записи. Формируется в сервисном слое.*/
    private String uuid;
    /*Наименование файла вместе с расширением*/
    private String name;
    /*Данные загружаемого файла*/
    private InputStream inputStream;
    /*Дата создания (sysdate в БД)*/
    private Date creationDate;

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

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
}
