package com.aplana.sbrf.taxaccounting.model.exception;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;

/**
 * Класс-исключение, использующийся когда на сервисном слое происходит ошибка связанная с тем, 
 * что скрипт выполнился с ошибками. 
 *
 * @author sgoryachkin
 *
 */
public class ServiceLoggerException extends ServiceException{
	private static final long serialVersionUID = -6734031798154470925L;
	
	private List<LogEntry> logEntries;

	public ServiceLoggerException(String message, List<LogEntry> logEntries, Object... params) {
		super(message, params);
		this.logEntries = logEntries;
	}

	public List<LogEntry> getLogEntries() {
		return logEntries;
	}

}
