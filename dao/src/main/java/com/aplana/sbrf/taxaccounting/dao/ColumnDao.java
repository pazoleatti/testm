package com.aplana.sbrf.taxaccounting.dao;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;

/**
 * Dao для работы с объявлениями столбцов формы
 * В первую очередь предназначено для использования при реализации {@link FormTemplateDao} 
 */
public interface ColumnDao {
	/**
	 * Получить список столбцов, входящих в заданную форму
	 * @param formId идентификатор формы
	 * @return список столбцов формы
	 */
	List<Column> getFormColumns(int formId);
	/**
	 * Сохранить список столбцов формы
	 * @param form форма
	 */
	void saveFormColumns(FormTemplate form);
}
