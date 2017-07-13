package com.aplana.sbrf.taxaccounting.web.paging;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс-обертка для данных при передаче в JqGrid
 *
 * @param <T> Параметром может быть любой объект. Например, entity класс
 */
public class JqgridPagedList<T> {

    // an array that contains the actual data
    private List<T> rows;

    // current page of the query
    private Integer page;

    // total pages for the query
    private Integer total;

    // total number of records for the query
    private Integer records;

    public JqgridPagedList() {
        rows = new ArrayList<T>();
    }

    public List<T> getRows() {
        return rows;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getRecords() {
        return records;
    }

    public void setRecords(Integer records) {
        this.records = records;
    }
}
