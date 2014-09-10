package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.*;

import java.util.List;

/**
 * Интерфейс для поиска по базе
 * @author srybakov
 *
 */
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

    List<Long> findIdsByFilter(FormDataDaoFilter filter);

    Long getRowNumByFilter(FormDataDaoFilter filter, FormDataSearchOrdering ordering, boolean ascSorting, Long formDataId);

	/**
	 * Возвращает список {@link FormDataSearchResultItem}, представляющий набор данных по налоговым формам, удовлетворяющих 
	 * переданному фильтру, с учётом заданной сортировки и заданному диапазону индексов (параметры паджинации) 
	 * Возвращается полный список данных, отсортированный по возрастанию id
	 * @param filter - фильтр, по которому происходит поиск
	 * @param ordering - способ сортировки
	 * @param ascSorting - true, если сортируем по возрастанию, false - по убыванию
	 * @param pageParams - диапазон индексов, задающий страницу
	 * @return возвращает объект {@link com.aplana.sbrf.taxaccounting.model.PagingResult}, содержащий информацию о результатах запроса
	 */
	PagingResult<FormDataSearchResultItem> findPage(FormDataDaoFilter filter, FormDataSearchOrdering ordering, boolean ascSorting, PagingParams pageParams);
	
	/**
	 * Получить количество записей, удовлетворяющих запросу.
     * Получать количество можно в один запрос, это делается в {@link #findPage}
	 * @param filter фильтр, по которому происходит поиск
	 * @return количество записей, удовлетворяющих фильтру
	 */
    @Deprecated
	int getCount(FormDataDaoFilter filter);
}
