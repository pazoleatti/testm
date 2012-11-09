package com.aplana.sbrf.taxaccounting.model.log;

import java.io.Serializable;

/**
 * Сообщение в журнале расчёта
 */
public class LogEntry implements Serializable {
	private static final long serialVersionUID = 1L;
	
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
