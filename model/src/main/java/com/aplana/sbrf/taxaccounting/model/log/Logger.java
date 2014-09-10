package com.aplana.sbrf.taxaccounting.model.log;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Объект для логирования информации в ходе расчётов, проверок и других операций
 * В ходе выполнения операции пользователь добавляет в журнал сообщения при помощи методов {@link #info(String, Object...)}, 
 * {@link #warn(String, Object...)}, {@link #error(String, Object...)}
 * Вся информация, записываемая в журал будет дублироваться в журнал сервера приложений средствами Commons.Logging
 */
public class Logger {
	private Log logger = LogFactory.getLog(getClass());
	
	private LogMessageDecorator messageDecorator;
	
	private List<LogEntry> entries = new ArrayList<LogEntry>();

    //Добавили пока на пробу, поскольку необходимо логгирование в справочнике Подразделений
    private TAUserInfo taUserInfo;

    public TAUserInfo getTaUserInfo() {
        return taUserInfo;
    }

    public void setTaUserInfo(TAUserInfo taUserInfo) {
        this.taUserInfo = taUserInfo;
    }

    // Ограничение по длине для каждого сообщения об ошибке из Exception
    private static final int MAX_EXCEPTION_LOG_MESSAGE_LENGTH = 10000;

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
	public void warn(String message, Object...args) {
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
        String msg = e.getMessage();
        if (msg != null && msg.length() > MAX_EXCEPTION_LOG_MESSAGE_LENGTH) {
            msg = msg.substring(0, MAX_EXCEPTION_LOG_MESSAGE_LENGTH - 1) + '…';
        }
        log(LogLevel.ERROR, "Ошибка: %s", msg);
		logger.error("Unhandled exception: " + msg, e);
	}

	private void log(LogLevel level, String message, Object...args) {
		String extMessage = String.format(message, args);
		if (messageDecorator != null) {
			extMessage = messageDecorator.getDecoratedMessage(extMessage);
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

	/**
	 * Установить декоратор для текста сообщений.
	 * Если установлен в null, то сообщения пишутся в неизменном виде,
	 * в противном случае к строке будет применено преобразование, выполняемое методом {@LogMessageDecorator#getDecoratedMessage} 
	 * @param messageDecorator декоратор, может быть null
	 */
	public void setMessageDecorator(LogMessageDecorator messageDecorator) {
		this.messageDecorator = messageDecorator;
	}

    public LogMessageDecorator getMessageDecorator() {
        return messageDecorator;
    }
	
	/**
	 * Очистить содержимое журнала
	 */
	public void clear() {
		entries.clear();
	}

    /**
     * Очистить содержимое журнала с определенным уровнем сообщений
     */
    public void clear(LogLevel logLevel) {
        Iterator<LogEntry> it = entries.iterator();
        while (it.hasNext()) {
            LogEntry entry = it.next();
            if (entry.getLevel().equals(logLevel)) {
                it.remove();
            }
        }
    }
}
