package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * Модель блокировок для отображения в таблице
 * @author dloshkarev
 */
public class LockDataItem implements Serializable {
    private static final long serialVersionUID = 2298941928955273347L;

    /* Идентификатор блокировки */
    private String key;
    /* Описание блокировки */
    private String description;
    /* Код пользователя, установившего блокировку*/
    private String userLogin;
    /* Дата истечения блокировки */
    private String dateBefore;
    /* Дата установки блокировки */
    private String dateLock;
    /* Состояние связанной асинхронной задачи */
    private String state;
    /* Дата последнего изменения состояния */
    private String stateDate;
    /* Очередь, в которой находится связанная асинхронная задача */
    private String queue;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    public String getDateBefore() {
        return dateBefore;
    }

    public void setDateBefore(String dateBefore) {
        this.dateBefore = dateBefore;
    }

    public String getDateLock() {
        return dateLock;
    }

    public void setDateLock(String dateLock) {
        this.dateLock = dateLock;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getStateDate() {
        return stateDate;
    }

    public void setStateDate(String stateDate) {
        this.stateDate = stateDate;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }
}
