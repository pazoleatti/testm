package com.aplana.sbrf.taxaccounting.web.paging;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;

import java.util.Collection;

public class JqgridPagedResourceAssembler {

    /**
     * Формирует объект {@code JqgridPagedList} для отправки на сторону клиента в JqGrid
     *
     * @param collection   объект {@code Collection}, содержащий ограниченный набор данных для запрошенной страницы
     * @param size         общее количество записей
     * @param pagingParams параметры для пагинации
     * @return {@code JqgridPagedList} подготовленный для JSON сериализации объект с данными в формате, ожидаемом JqGrid.JsonReader
     */
    public static <T> JqgridPagedList<T> buildPagedList(Collection<T> collection, Integer size, PagingParams pagingParams) {
        JqgridPagedList<T> pagedList = new JqgridPagedList<>();
        pagedList.getRows().addAll(collection);

        pagedList.setRecords(size);
        if (pagingParams != null) {
            pagedList.setPage(pagingParams.getPage());
            Integer total = new Double(Math.ceil((double) size / pagingParams.getCount())).intValue();
            pagedList.setTotal(total == 0 ? 1 : total);
        } else {
            pagedList.setPage(1);
            pagedList.setTotal(collection.size());
        }

        return pagedList;
    }

    /**
     * Формирует объект {@code JqgridPagedList} из коллекции типа {@code PagingResult}.
     */
    public static <T> JqgridPagedList<T> buildPagedList(PagingResult<T> pagedList, PagingParams pagingParams) {
        return buildPagedList(pagedList, pagedList.getTotalCount(), pagingParams);
    }
}
