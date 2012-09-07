package com.aplana.sbrf.taxaccounting.dao.exсeption;

/**
 * Класс-исключение, используется, когда при выполнении операции по работе с БД
 * произошла ошибка, требующая отката транзакции. 
 * Класс должен использоваться для генерации ошибок, определяющихся нарушением бизнес-логики,
 * а не техническими проблемами.
 * Текст сообщения должен быть понятен пользователю
 */
public class DaoException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	/**
	 * Создаёт объект исключения с заданным текстом ошибки. Текст может содержать плейсхолдеры,
	 * которые будут заполнены на основе значений, переданных в последующих опциональных аргументах.
	 * Сигнатура метода повторяет метод {@link String#format(String, Object...)} 
	 * @param message текст ошибки, опционально содержащий плейсхолдеры
	 * @param params список объектов для подставноки в текст сообщения
	 */
	public DaoException(String message, Object... params) {
		super(String.format(message, params));
	}
}
