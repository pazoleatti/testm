package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Модель блокировок для отображения в таблице
 * @author dloshkarev
 */
public class LockDataItem implements Serializable {
    private static final long serialVersionUID = 2298941928955273347L;

    /* Идентификатор блокировки */
    private long id;
    /* Ключ блокировки */
    private String key;
    /* Описание блокировки */
    private String description;
    /* Полное имя пользователя, установившего блокировку*/
    private String user;
    /* Дата установки блокировки */
    private Date dateLock;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Date getDateLock() {
        return dateLock;
    }

    public void setDateLock(Date dateLock) {
        this.dateLock = dateLock;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
