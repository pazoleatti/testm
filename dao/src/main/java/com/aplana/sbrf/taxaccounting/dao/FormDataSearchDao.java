package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.FormDataDaoFilter;
import com.aplana.sbrf.taxaccounting.model.FormDataSearchResultItem;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

import java.util.List;

/**
 * Интерфейс для поиска по базе
 * @author srybakov
 *
 */
public interface FormDataSearchDao extends ScriptExposed {

	/**
	 * Возвращает список идентификаторов данных по налоговым формам, удовлетворяющие запросу
	 * @param filter - фильтр, по которому происходит поиск
	 * @return возвращает информацию по всем имеющимся в наличии заполенным формам, удовлетворяющие
	 * запросу
	 */
	List<FormDataSearchResultItem> findByFilter(FormDataDaoFilter filter);

}
