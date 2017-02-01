package com.aplana.sbrf.taxaccounting.model.log;

import java.util.Date;

/**
 * Сущность для группы логов
 *
 * @author pmakarov
 */
public class LogEntity {

    /**
     * Идентификатор группы сообщений {@link java.util.UUID}
     */
    private String logId;

    /**
     * Идентификатор пользователя
     */
    private Long userId;

    /**
     * Дата создания сообщения
     */
    private Date creationDate;

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LogEntity logEntity = (LogEntity) o;

        if (logId != null ? !logId.equals(logEntity.logId) : logEntity.logId != null) return false;
        if (userId != null ? !userId.equals(logEntity.userId) : logEntity.userId != null) return false;
        return creationDate != null ? creationDate.equals(logEntity.creationDate) : logEntity.creationDate == null;
    }

    @Override
    public int hashCode() {
        int result = logId != null ? logId.hashCode() : 0;
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (creationDate != null ? creationDate.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Log{" +
                "logId='" + logId + '\'' +
                ", userId=" + userId +
                ", creationDate=" + creationDate +
                '}';
    }
}
