package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.ColumnKeyEnum;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;

import java.util.Collection;
import java.util.List;
import java.util.Map;

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
    Map<ColumnKeyEnum, Collection<Long>> updateFormColumns(FormTemplate form);
    /**
     * Получить список атрибутов второго уровня для атрибута который используется в колонках.
     * @param attributes атрибуты
     * @return карта из списка атрибутов второго уровня всех колонок где используются attributes. Всегда возвращает список.
	 * В списке не для всех атрибутов могут присутствовать списки атрибутов второго уровня
     */
	Map<Long, List<Long>> getAttributeId2(List<RefBookAttribute> attributes);

    int getColumnIdByAlias(int formTemplateId, String columnAlias);
}