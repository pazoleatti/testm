package com.aplana.sbrf.taxaccounting.model.log;

import java.io.Serializable;
import java.util.Date;

/**
 * Сущность для группы логов.
 * Повторяет функциональность класса LogEntry {@link com.aplana.sbrf.taxaccounting.model.log.LogEntry}
 * используется только для совместимости с виджетами GWT
 **/

public final class GWTLogEntry implements Serializable {

    private static final int MAX_TYPE_LENGTH = 255;
    private static final int MAX_OBJECT_LENGTH = 255;

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
     * Тип
     */
    private String type;

    /**
     * Объект
     */
    private String object;

    public GWTLogEntry() {
    }

    /**
     * Конструктор для создания сообщения
     *
     * @param level   уровень важности сообщения
     * @param message текст сообщения
     */
    public GWTLogEntry(LogLevel level, String message) {
        this.level = level;
        setMessage(message);
        this.date = new Date();
    }

    public GWTLogEntry(LogLevel level, String message, String type, String object) {
        this.level = level;
        setMessage(message);
        setType(type);
        setObject(object);
        this.date = new Date();
    }

    /**
     * Конструктор копирования
     * Используется при разбиение одного большого сообщения на маленькие
     *
     * @param logEntry запись из которой скопировать значения
     */
    public GWTLogEntry(GWTLogEntry logEntry) {
        this.logId = logEntry.logId;
        this.ord = logEntry.ord;
        this.date = logEntry.date;
        this.level = logEntry.level;
        this.message = logEntry.message;
        this.type = logEntry.type;
        this.object = logEntry.object;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }
}
