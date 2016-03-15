package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.FormStyle;

/**
 * Сервис для работы со стилями ячеек НФ
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 15.03.2016 17:45
 */
public interface StyleService {

	String STYLE_NO_CHANGE = "Корректировка-без изменений";
	String STYLE_INSERT = "Корректировка-добавлено";
	String STYLE_DELETE = "Корректировка-удалено";
	String STYLE_CHANGE = "Корректировка-изменено";

	String EDITABLE_CELL_STYLE = "Редактируемая";
	String AUTO_FILL_CELL_STYLE = "Автозаполняемая";

	/**
	 * Получить стиль по его алиасу
	 * @param alias алиас стиля.
	 * @return стиль
	 */
	FormStyle get(String alias);
}