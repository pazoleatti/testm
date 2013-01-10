package com.aplana.sbrf.taxaccounting.dao;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.FormDataDaoFilter;
import com.aplana.sbrf.taxaccounting.model.FormDataSearchOrdering;
import com.aplana.sbrf.taxaccounting.model.FormDataSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.PaginatedSearchParams;
import com.aplana.sbrf.taxaccounting.model.PaginatedSearchResult;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

/**
 * Интерфейс для поиска по базе
 * @author srybakov
 *
 */
@ScriptExposed
public interface FormDataSearchDao {

	/**
	 * Возвращает список {@link FormDataSearchResultItem}, представляющий набор данных по налоговым формам, удовлетворяющих 
	 * переданному фильтру
	 * Возвращается полный список данных, отсортированный по возрастанию id
	 * @param filter - фильтр, по которому происходит поиск
	 * @return возвращает информацию по всем имеющимся в наличии заполенным формам, удовлетворяющим
	 * запросу
	 */
	List<FormDataSearchResultItem> findByFilter(FormDataDaoFilter filter);
	
	/**
	 * Возвращает список {@link FormDataSearchResultItem}, представляющий набор данных по налоговым формам, удовлетворяющих 
	 * переданному фильтру, с учётом заданной сортировки и заданному диапазону индексов (параметры паджинации) 
	 * Возвращается полный список данных, отсортированный по возрастанию id
	 * @param filter - фильтр, по которому происходит поиск
	 * @param ordering - способ сортировки
	 * @param ascSorting - true, если сортируем по возрастанию, false - по убыванию
	 * @param pageParams - диапазон индексов, задающий страницу
	 * @return возвращает объект {@link PaginatedSearchResult}, содержащий информацию о результатах запроса
	 */
	PaginatedSearchResult<FormDataSearchResultItem> findPage(FormDataDaoFilter filter, FormDataSearchOrdering ordering, boolean ascSorting, PaginatedSearchParams pageParams);
	
	/**
	 * Получить количество записей, удовлетворяющих запросу
	 * @param filter фильтр, по которому происходит поиск
	 * @return количество записей, удовлетворяющих фильтру
	 */
	long getCount(FormDataDaoFilter filter);
}
