package com.aplana.sbrf.taxaccounting.web.paging;

import java.util.Collection;

import org.springframework.data.domain.Pageable;

public class JqgridPagedResourceAssembler {

    /**
     * Формирует объект {@code JqgridPagedList} для отправки на сторону клиента в JqGrid
     *
     * @param collection объект {@code Collection}, содержащий ограниченный набор данных для запрошенной страницы
     * @param size       общее количество записей
     * @param pageable   объект {@code Pageable}, полученный из {@code pageableResolver}
     * @return {@code JqgridPagedList} подготовленный для JSON сериализации объект с данными в формате, ожидаемом JqGrid.JsonReader
     */
    public static <T> JqgridPagedList<T> buildPagedList(Collection<T> collection, Integer size, Pageable pageable) {
        JqgridPagedList<T> pagedList = new JqgridPagedList<T>();
        pagedList.getRows().addAll(collection);

        pagedList.setPage(pageable.getPageNumber());
        pagedList.setRecords(size);
        pagedList.setTotal(new Double(Math.ceil((double) size / pageable.getPageSize())).intValue());

        return pagedList;
    }

    /**
     * Формирует объект {@code JqgridPagedList} для отправки коллекции целиком на сторону клиента в JqGrid
     *
     * @param collection объект {@code Collection}, содержащий весь набор данных
     * @return {@code JqgridPagedList} подготовленный для JSON сериализации объект с данными в формате, ожидаемом JqGrid.JsonReader
     */
    public static <T> JqgridPagedList<T> buildPagedList(Collection<T> collection) {
        JqgridPagedList<T> pagedList = new JqgridPagedList<T>();
        pagedList.getRows().addAll(collection);

        pagedList.setPage(1);
        pagedList.setRecords(collection.size());
        pagedList.setTotal(1);

        return pagedList;
    }
}
