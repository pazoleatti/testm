package com.aplana.sbrf.taxaccounting.web.main.api.client.sortable;

import com.gwtplatform.mvp.client.View;

/**
 * @author Fail Mukhametdinov
 */
public interface ViewWithSortableTable extends View {
    /**
     * Метод возвращает направление сортировки: по возрастающей или убывающей.
     *
     * @return по возрастающей - true, иначе - false
     */
    boolean isAscSorting();

    /**
     * Задать сортировку по конкретному столбцу.
     *
     * @param dataStoreName название столбца
     */
    void setSortByColumn(String dataStoreName);
}
