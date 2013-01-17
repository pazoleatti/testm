package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.FormStyle;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;

import java.util.List;

/**
 * Dao для работы с коллекции стилей
 * В первую очередь предназначено для использования при реализации {@link FormTemplateDao}
 */
public interface FormStyleDao {

	/**
	 * Получить список стилей, для данной формы
	 * @param formId идентификатор формы
	 * @return список стилей формы
	 */
	List<FormStyle> getFormStyles(int formId);
	/**
	 * Сохранить список стилей формы
	 * @param form форма
	 */
	void saveFormStyles(FormTemplate form);
}
