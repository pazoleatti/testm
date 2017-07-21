package com.aplana.sbrf.taxaccounting.web.paging;

import java.util.Collection;

import org.springframework.data.domain.Pageable;

public class JqgridPagedResourceAssembler {

    /**
     * Формирует объект {@code JqgridPagedList} для отправки на сторону клиента в JqGrid
     *
     * @param collection объект {@code Collection}, содержащий ограниченный набор данных для запрошенной страницы
     * @param size       общее количество записей
     * @param page номер страницы
     * @param rows количество записей на одной странице
     * @return {@code JqgridPagedList} подготовленный для JSON сериализации объект с данными в формате, ожидаемом JqGrid.JsonReader
     */
    public static <T> JqgridPagedList<T> buildPagedList(Collection<T> collection, Integer size, int page, int rows) {
        JqgridPagedList<T> pagedList = new JqgridPagedList<T>();
        pagedList.getRows().addAll(collection);

        pagedList.setPage(page);
        pagedList.setRecords(size);
        pagedList.setTotal(new Double(Math.ceil((double) size / rows)).intValue());

        return pagedList;
    }
}
