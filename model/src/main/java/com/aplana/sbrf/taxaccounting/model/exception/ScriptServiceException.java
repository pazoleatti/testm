package com.aplana.sbrf.taxaccounting.model.exception;

/**
 * Класс-исключение, использующийся когда в скриптах нужно выбросить исключение,
 * опознаваемое как "предвиденная" фатальная ошибка
 * @author lhaziev
 */
public class ScriptServiceException extends ServiceException {
	private static final long serialVersionUID = 1L;

	public ScriptServiceException(String message, Object... params) {
		super(message, params);
	}	
}
