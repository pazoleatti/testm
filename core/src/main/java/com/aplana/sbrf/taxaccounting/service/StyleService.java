package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.FormStyle;

import java.util.List;

/**
 * Сервис для работы со стилями ячеек НФ
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 15.03.2016 17:45
 */
public interface StyleService {

	/**
	 * Получить стиль по его алиасу
	 * @param alias алиас стиля.
	 * @return стиль
	 */
	FormStyle get(String alias);
	/**
	 * Получить все стили, включая встроенный "По умолчанию"
	 * @return
	 */
	List<FormStyle> getAll();
}