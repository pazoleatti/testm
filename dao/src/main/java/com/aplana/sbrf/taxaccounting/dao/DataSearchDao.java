package com.aplana.sbrf.taxaccounting.dao;


import com.aplana.sbrf.taxaccounting.dao.model.Filter;
import com.aplana.sbrf.taxaccounting.model.FormData;

import java.util.List;

/**
 * Интерфейс для поиска по базе
 * @author srybakov
 *
 */
public interface DataSearchDao {

	/**
	 * Возвращает список идентификаторов данных по налоговым формам, имеющих указанный тип
	 * @param filter - фильтр, по которому происходит поиск
	 * @return возвращает информацию по всем имеющимся в наличии заполенным формам, удовлетворяющие
	 * запросу
	 */
	List<FormData> findByFilter(Filter filter);

}
