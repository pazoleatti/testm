package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.FormStyle;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;

import java.util.Collection;
import java.util.List;
import java.util.Map;

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
	 * Получить мапку, где ключ - id стиля, значение - стиль
	 * @param formTemplateId идентификатор формы
	 * @return мапка
	 */
	Map<Integer, FormStyle> getIdToFormStyleMap(int formTemplateId);

	/**
	 * Получить мапку, где ключ - alias стиля, значение - стиль
	 * @param formTemplateId идентификатор формы
	 * @return мапка
	 */
	Map<String, FormStyle> getAliasToFormStyleMap(int formTemplateId);

	/**
	 * Сохранить список стилей формы
	 * @param formTemplate форма
	 */
	Collection<Integer> saveFormStyles(FormTemplate formTemplate);
}
