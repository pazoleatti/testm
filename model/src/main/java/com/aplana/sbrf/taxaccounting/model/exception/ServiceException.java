package com.aplana.sbrf.taxaccounting.model.exception;

/**
 * Класс-исключение, используется, когда при выполнении операции сервисного слоя произошла ошибка. 
 * Класс должен использоваться для генерации ошибок, определяющихся нарушением бизнес-логики,
 * а не техническими проблемами.
 * Текст сообщения должен быть понятен пользователю
 */
public class ServiceException extends TAException {
	private static final long serialVersionUID = 1L;

	public ServiceException() {
		super();
	}

	public ServiceException(String message, Object... params) {
		super(message, params);
	}
	
	public ServiceException(String message, Throwable cause) {
		super(message, cause);
	}
}
