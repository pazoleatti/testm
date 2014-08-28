package com.aplana.sbrf.taxaccounting.model;

/**
 * Фильтр для сортировки
 *
 * @author Fail Mukhametdinov
 */
public class QueryParams<T extends Enum<T>> {
    //true, если сортируем по возрастанию, false - по убыванию
    private boolean ascending;

    // Столбец, по которому сортируем
    private Enum<T> searchOrdering;

    /**
     * Параметр пейджинга - от
     */
    private int from;

    /**
     * Количество записей которые нужно вернуть
     */
    private int count;


    public Enum<T> getSearchOrdering() {
        return searchOrdering;
    }

    public void setSearchOrdering(Enum<T> searchOrdering) {
        this.searchOrdering = searchOrdering;
    }

    public boolean isAscending() {
        return ascending;
    }

    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
