package com.aplana.sbrf.taxaccounting.model.log;

import java.io.Serializable;
import java.util.Date;

/**
 * Сообщение в журнале расчёта
 */
public class LogEntry implements Serializable {
	private static final long serialVersionUID = 1L;

    private Date date;
	private LogLevel level;
	private String message;
	
	/**
	 * Конструктор по-умолчанию
	 * Напрямую использоваться не должен, создан для совместимости с GWT
	 */
	public LogEntry() {
	}
	
	/**
	 * Конструктор для создания сообщения
	 * @param level уровень важности сообщения
	 * @param message текст сообщения
	 */
	public LogEntry(LogLevel level, String message) {
		this.level = level;
		this.message = message;
        this.date = new Date();
	}

    /**
     *
     * @return дата и время формирования сообщения
     */
    public Date getDate() {
        return date;
    }

	/**
	 * @return уровень важности сообщения
	 */
	public LogLevel getLevel() {
		return level;
	}
	/**
	 * @return текст сообщения
	 */
	public String getMessage() {
		return message;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LogEntry entry = (LogEntry) o;

        if (level != entry.level) return false;
        if (message != null ? !message.equals(entry.message) : entry.message != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = level != null ? level.hashCode() : 0;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        return result;
    }

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("LogEntry{");
		sb.append("date=").append(date);
		sb.append(", level=").append(level);
		sb.append(", message='").append(message).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
