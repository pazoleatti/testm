package com.aplana.sbrf.taxaccounting.model.exception;

/**
 * Базовый класс для исключений, создаваемых в рамках проекта.
 * Предполагается, что исключения, отнаследованные от этого объекта, должны содержать текст с описанием ошибки,
 * понятный пользователю.
 * 
 * NOTE: Это не означает, что все исключения, выбрасываемые в проекте должны быть отнаследованы от TAException.
 * Во многих случаях вполне можно использовать "стандартные" классы исключений - NullPointerException, IllegalArgumentException и т.д. 
 * @author dsultanbekov
 */
public abstract class TAException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Создаёт объект исключения с заданным текстом ошибки. Текст может содержать плейсхолдеры,
	 * которые будут заполнены на основе значений, переданных в последующих опциональных аргументах.
	 * Сигнатура метода повторяет метод {@link String#format(String, Object...)} 
	 * @param message текст ошибки, опционально содержащий плейсхолдеры
	 * @param params список объектов для подставноки в текст сообщения
	 */
	public TAException(String message, Object... params){
		super(String.format(message, params));
	}
	
	public TAException(String message, Throwable cause){
		super(message, cause);
	}
	
	
}
