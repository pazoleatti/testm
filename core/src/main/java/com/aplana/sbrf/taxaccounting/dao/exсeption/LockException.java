package com.aplana.sbrf.taxaccounting.dao.exсeption;

/**
 * Исключение возникающее, если попытка заблокировать или разблокировать объект не удалась.
 * Причины неудачи отражаются в тексте сообщения
 * @author dsultanbekov
 */
public class LockException extends DaoException {
	private static final long serialVersionUID = 1L;
	
	public LockException(String message, Object... params) {
		super(message, params);
	}
}
