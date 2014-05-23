package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.*;

/**
 * Интерфейс для поиска бух. отчетностей по базе
 * @author lhaziev
 */
public interface BookerStatementsSearchDao {
    /**
     * Возвращает список {@link com.aplana.sbrf.taxaccounting.model.BookerStatementsSearchResultItem}, представляющий набор данных по налоговым формам, удовлетворяющих
     * переданному фильтру, с учётом заданной сортировки и заданному диапазону индексов (параметры паджинации)
     * Возвращается полный список данных, отсортированный по возрастанию id
     * @param filter - фильтр, по которому происходит поиск
     * @param ordering - способ сортировки
     * @param ascSorting - true, если сортируем по возрастанию, false - по убыванию
     * @param pageParams - диапазон индексов, задающий страницу
     * @return возвращает объект {@link com.aplana.sbrf.taxaccounting.model.PagingResult}, содержащий информацию о результатах запроса
     */
    PagingResult<BookerStatementsSearchResultItem> findPage(BookerStatementsFilter filter, BookerStatementsSearchOrdering ordering, boolean ascSorting, PagingParams pageParams);

}
