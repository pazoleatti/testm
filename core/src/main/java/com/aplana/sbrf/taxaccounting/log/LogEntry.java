package com.aplana.sbrf.taxaccounting.log;

import java.io.Serializable;

/**
 * Сообщение в журнале расчёта
 */
public class LogEntry implements Serializable {
	private LogLevel level;
	private String message;
	
	/**
	 * Конструктор для создания сообщения
	 * @param level уровень важности сообщения
	 * @param message текст сообщения
	 */
	public LogEntry(LogLevel level, String message) {
		this.level = level;
		this.message = message;
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
}
