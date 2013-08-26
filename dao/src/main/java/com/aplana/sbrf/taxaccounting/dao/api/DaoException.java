package com.aplana.sbrf.taxaccounting.dao.api;

import com.aplana.sbrf.taxaccounting.model.exception.TAException;

/**
 * Класс-исключение, используется, когда при выполнении операции по работе с БД
 * произошла ошибка, требующая отката транзакции. 
 * Класс должен использоваться для генерации ошибок, определяющихся нарушением бизнес-логики,
 * а не техническими проблемами.
 * Текст сообщения должен быть понятен пользователю
 */
public class DaoException extends TAException {
	private static final long serialVersionUID = 1L;

	public DaoException(String message, Object... params) {
		super(message, params);
	}
}
