package com.aplana.sbrf.taxaccounting.model;

import java.util.Date;

/**
 * Модельный класс с информацией о блокировке
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 17.07.14 13:51
 */

public final class LockData implements SecuredEntity {

    public static final String STANDARD_LOCK_MSG = "Объект заблокирован другой операцией. Попробуйте выполнить операцию позже";

    /* Идентификатор блокировки */
    private long id;
    /* Идентификатор блокировки */
    private String key;
    /* Код пользователя, установившего блокировку*/
    private int userId;
    /* Дата установки блокировки */
    private Date dateLock;
    /* Описание блокировки */
    private String description;
    /* Идентификатор асинхронной задачи, связанной с блокировкой */
    private Long taskId;

    /* Права */
    private long permissions;

    public enum LockObjects {
        REF_BOOK,
        DECLARATION_DATA,
        DECLARATION_CREATE,
        DECLARATION_TEMPLATE,
        FILE,
        CONFIGURATION_PARAMS,
        LOAD_TRANSPORT_DATA,
        EXCEL_TEMPLATE_DECLARATION,
        IMPORT_DECLARATION_EXCEL,
        IMPORT_REF_BOOK_XML
    }

    public LockData() {
    }

    public LockData(String key, int userId) {
        this.key = key;
        this.userId = userId;
    }

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

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
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

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    @Override
    public long getPermissions() {
        return permissions;
    }

    @Override
    public void setPermissions(long permissions) {
        this.permissions = permissions;
    }

    @Override
    public String toString() {
        return "LockData{" +
                "key='" + key + '\'' +
                ", userId=" + userId +
                ", dateLock=" + dateLock +
                ", description='" + description + '\'' +
                ", taskId=" + taskId +
                ", permissions=" + permissions +
                '}';
    }
}