package com.aplana.sbrf.taxaccounting.model.log;

import java.io.Serializable;
import java.util.Date;

/**
 * Сообщение в журнале
 */
public class LogEntry implements Serializable {

    /**
     * Идентификатор группы сообщений {@link java.util.UUID}
     */
    private String logId;

    /**
     * Порядковый номер сообщения в группе
     */
    private int ord;

    /**
     * Дата и время формирования сообщения
     */
    private Date date;

    /**
     * Уровень важности сообщения
     */
    private LogLevel level;

    /**
     * Текст сообщения
     */
    private String message;

    /**
     * Конструктор по-умолчанию
     * Напрямую использоваться не должен, создан для совместимости с GWT
     */
    public LogEntry() {
    }

    /**
     * Конструктор для создания сообщения
     *
     * @param level   уровень важности сообщения
     * @param message текст сообщения
     */
    public LogEntry(LogLevel level, String message) {
        this.level = level;
        this.message = message;
        this.date = new Date();
    }

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public int getOrd() {
        return ord;
    }

    public void setOrd(int ord) {
        this.ord = ord;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public LogLevel getLevel() {
        return level;
    }

    public void setLevel(LogLevel level) {
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LogEntry logEntry = (LogEntry) o;

        if (ord != logEntry.ord) return false;
        if (logId != null ? !logId.equals(logEntry.logId) : logEntry.logId != null) return false;
        if (date != null ? !date.equals(logEntry.date) : logEntry.date != null) return false;
        if (level != logEntry.level) return false;
        return message != null ? message.equals(logEntry.message) : logEntry.message == null;
    }

    @Override
    public int hashCode() {
        int result = logId != null ? logId.hashCode() : 0;
        result = 31 * result + ord;
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (level != null ? level.hashCode() : 0);
        result = 31 * result + (message != null ? message.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "LogEntry{" +
                "logId=" + logId +
                ", ord=" + ord +
                ", date=" + date +
                ", level=" + level +
                ", message='" + message + '\'' +
                '}';
    }
}
