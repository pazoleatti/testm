package com.aplana.sbrf.taxaccounting.model.exception;

/**
 * Класс-исключение, используется для отката вложенной транзакции, при этом данное исключение в дальнейшем перехватывается
 * и не откатывает исходную транзакцию.
 */
public class ServiceRollbackException extends TAException {
	private static final long serialVersionUID = 1L;

	public ServiceRollbackException() {
		super();
	}

	public ServiceRollbackException(String message, Object... params) {
		super(message, params);
	}

	public ServiceRollbackException(String message, Throwable cause) {
		super(message, cause);
	}
}
