package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.FormStyle;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import java.util.List;

/**
 * ДАО для работы со стилями ячеек НФ
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 21.03.2016 15:29
 */
public interface StyleDao {
	/**
	 * Получить стиль по его алиасу
	 * @param alias алиас стиля.
	 * @return стиль
	 * @throws IncorrectResultSizeDataAccessException если стиль не найден
	 */
	FormStyle get(String alias);
	/**
	 * Получить все стили
	 * @return
	 */
	List<FormStyle> getAll();
}