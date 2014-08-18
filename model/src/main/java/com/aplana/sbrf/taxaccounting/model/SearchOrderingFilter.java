package com.aplana.sbrf.taxaccounting.model;

/**
 * Фильтр для сортировки
 *
 * @author Fail Mukhametdinov
 */
public class SearchOrderingFilter<T extends Enum<T>> {
    //true, если сортируем по возрастанию, false - по убыванию
    private boolean ascSorting;
    // Столбец, по которому сортируем
    private Enum<T> searchOrdering;

    public boolean isAscSorting() {
        return ascSorting;
    }

    public void setAscSorting(boolean ascSorting) {
        this.ascSorting = ascSorting;
    }

    public Enum<T> getSearchOrdering() {
        return searchOrdering;
    }

    public void setSearchOrdering(Enum<T> searchOrdering) {
        this.searchOrdering = searchOrdering;
    }
}
