package com.aplana.sbrf.taxaccounting.web.paging;

import java.util.Collection;

import com.aplana.sbrf.taxaccounting.model.PagingParams;

public class JqgridPagedResourceAssembler {

    /**
     * Формирует объект {@code JqgridPagedList} для отправки на сторону клиента в JqGrid
     *
     * @param collection объект {@code Collection}, содержащий ограниченный набор данных для запрошенной страницы
     * @param size       общее количество записей
     * @param pagingParams параметры для пагинации
     * @return {@code JqgridPagedList} подготовленный для JSON сериализации объект с данными в формате, ожидаемом JqGrid.JsonReader
     */
    public static <T> JqgridPagedList<T> buildPagedList(Collection<T> collection, Integer size, PagingParams pagingParams) {
        JqgridPagedList<T> pagedList = new JqgridPagedList<T>();
        pagedList.getRows().addAll(collection);

        pagedList.setPage(pagingParams.getPage());
        pagedList.setRecords(size);
        pagedList.setTotal(new Double(Math.ceil((double) size / pagingParams.getCount())).intValue());

        return pagedList;
    }
}
