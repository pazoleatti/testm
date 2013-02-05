package com.aplana.sbrf.taxaccounting.exception;

/**
 * Класс-исключение, использующийся когда на сервисном слое происходит ошибка,
 * вызванная нехваткой прав у пользователя
 * @author dsultanbekov
 */
public class AccessDeniedException extends ServiceException {
	private static final long serialVersionUID = 1L;

	public AccessDeniedException(String message, Object... params) {
		super(message, params);
	}	
}
