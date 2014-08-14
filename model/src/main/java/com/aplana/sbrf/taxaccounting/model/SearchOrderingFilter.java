package com.aplana.sbrf.taxaccounting.model;

/**
 * Фильтр для сортировки
 *
 * @author Fail Mukhametdinov
 */
public class SearchOrderingFilter {
    //true, если сортируем по возрастанию, false - по убыванию
    private boolean ascSorting;
    // Столбец, по которому сортируем
    private SearchOrdering searchOrdering;

    public boolean isAscSorting() {
        return ascSorting;
    }

    public void setAscSorting(boolean ascSorting) {
        this.ascSorting = ascSorting;
    }

    public SearchOrdering getSearchOrdering() {
        return searchOrdering;
    }

    public void setSearchOrdering(SearchOrdering searchOrdering) {
        this.searchOrdering = searchOrdering;
    }
}
