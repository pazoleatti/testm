package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.gwtplatform.dispatch.shared.ActionException;

/**
 * Класс-исключение, используется, когда при выполнении операции сервисного слоя произошла ошибка.
 * Используется при некорректных параметрах сервиса.
 * Текст сообщения должен быть понятен пользователю
 */
public class WrongInputDataServiceException extends ActionException {
	private static final long serialVersionUID = 1L;

	public WrongInputDataServiceException() {
		super();
	}

	public WrongInputDataServiceException(String msg) {
		super(msg);
	}
}
