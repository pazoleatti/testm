package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.dao.model.FormDataDaoFilter;
import com.aplana.sbrf.taxaccounting.model.FormData;

import java.util.List;

/**
 * Интерфейс для поиска по базе
 * @author srybakov
 *
 */
public interface FormDataSearchDao {

	/**
	 * Возвращает список идентификаторов данных по налоговым формам, удовлетворяющие запросу
	 * @param filter - фильтр, по которому происходит поиск
	 * @return возвращает информацию по всем имеющимся в наличии заполенным формам, удовлетворяющие
	 * запросу
	 */
	List<FormData> findByFilter(FormDataDaoFilter filter);

}
