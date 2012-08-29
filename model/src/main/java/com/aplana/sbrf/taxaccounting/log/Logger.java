package com.aplana.sbrf.taxaccounting.log;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Объект для логирования информации в ходе расчётов, проверок и других операций
 * В ходе выполнения операции пользователь добавляет в журнал сообщения при помощи методов {@link #info(String, Object...)}, 
 * {@link #warning(String, Object...)}, {@link #error(String, Object...)}
 * Вся информация, записываемая в журал будет дублироваться в журнал сервера приложений средствами Commons.Logging
 */
public class Logger {
	private Log logger = LogFactory.getLog(getClass());
	
	private List<LogEntry> entries = new ArrayList<LogEntry>();
	/**
	 * Добавить информационное сообщение в журнал (это сообщения, не требующие особой реакции пользователя) 
	 * @param message строка сообщения, может содержать плейсхолдеры, аналогичные используемым в методе {@link String#format(String, Object...)} 
	 * @param args набор объектов для подставновки в текст сообщения, может не задаваться
	 */
	public void info(String message, Object... args) {
		log(LogLevel.INFO, message, args);
	}
	/**
	 * Добавить предупреждающее сообщение в журнал (работа системы на нарушена, но нужно обратить внимание пользователя на что-то) 
	 * @param message строка сообщения, может содержать плейсхолдеры, аналогичные используемым в методе {@link String#format(String, Object...)} 
	 * @param args набор объектов для подставновки в текст сообщения, может не задаваться
	 */
	public void warning(String message, Object...args) {
		log(LogLevel.WARNING, message, args);
	}
	/**
	 * Добавить сообщение об ошибке в журнал (ошибка, требующая вмешательства пользователя для корректной работы системы) 
	 * @param message строка сообщения, может содержать плейсхолдеры, аналогичные используемым в методе {@link String#format(String, Object...)} 
	 * @param args набор объектов для подставновки в текст сообщения, может не задаваться
	 */
	public void error(String message, Object...args) {
		log(LogLevel.ERROR, message, args);
	}
	
	/**
	 * Записать сообщение о неожиданном исключении в журнал
	 * В журнал сервера приложений будет выведен стектрейс ошибки
	 * @param e исключение
	 */
	public void error(Exception e) {
		log(LogLevel.ERROR, "При выполнении операции произошла непредвиденная ошибка: %s", e.getMessage());
		logger.error("Unexpected exception: " + e.getMessage(), e);
	}

	private void log(LogLevel level, String message, Object...args) {
		String extMessage = String.format(message, args);
		
		if (level == LogLevel.ERROR) {
			
		}
		
		LogEntry entry = new LogEntry(level, extMessage);
		entries.add(entry);
	}
	
	/**
	 * Метод проверяет, что в журнале содержится хотя бы одно сообщение с указанным уровнем важности
	 * @param level уровень важности 
	 * @return true, если сообщения есть, false - в противном случае
	 */
	public boolean containsLevel(LogLevel level) {
		for (LogEntry entry: entries) {
			if (entry.getLevel() == level) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Получить сообщения, записанные в журнал
	 * @return все сообщения, записанные в журнал в порядке их добавления
	 */
	public List<LogEntry> getEntries() {
		return entries;
	}
}
