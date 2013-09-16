package com.aplana.sbrf.taxaccounting.dao.api.exception;

import com.aplana.sbrf.taxaccounting.model.exception.TAException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Класс-исключение, используется, когда при выполнении операции по работе с БД
 * произошла ошибка, требующая отката транзакции. 
 * Класс должен использоваться для генерации ошибок, определяющихся нарушением бизнес-логики,
 * а не техническими проблемами.
 * Текст сообщения должен быть понятен пользователю
 */
//TODO: надо переделать класс DaoException и его предков. Сделать проще без "чудо"-конструкторов (Marat Fayzullin 2013-09-15)
public class DaoException extends TAException {
	private static final long serialVersionUID = 1L;

	private static final Log logger = LogFactory.getLog(DaoException.class);

	public DaoException(String message, Object... params) {
		super(message, params);
	}

	public DaoException(String message, Throwable cause){
		super(message, cause);
		logger.error(cause.getMessage(), cause);
	}
}
