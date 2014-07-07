package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author ibukanov
 *         Класс используется для поиска данных по журналу аудита
 */
public class LogSystemFilter implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Long> auditFieldList;
    private String filter;
    private LogSystemFilter oldLogSystemFilter;
    private Date fromSearchDate;
    private Date toSearchDate;

    /*Стартовый индекс списка записей */
    private int startIndex;

    /*Количество записей, которые нужно вернуть*/
    private int countOfRecords;

    private HistoryBusinessSearchOrdering searchOrdering;

    /*true, если сортируем по возрастанию, false - по убыванию*/
    private boolean ascSorting;

    public List<Long> getAuditFieldList() {
        return auditFieldList;
    }

    public void setAuditFieldList(List<Long> auditFieldList) {
        this.auditFieldList = auditFieldList;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public Date getFromSearchDate() {
        return fromSearchDate;
    }

    public void setFromSearchDate(Date fromSearchDate) {
        this.fromSearchDate = fromSearchDate;
    }

    public Date getToSearchDate() {
        return toSearchDate;
    }

    public void setToSearchDate(Date toSearchDate) {
        this.toSearchDate = toSearchDate;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getCountOfRecords() {
        return countOfRecords;
    }

    public void setCountOfRecords(int countOfRecords) {
        this.countOfRecords = countOfRecords;
    }

    public HistoryBusinessSearchOrdering getSearchOrdering() {
        return searchOrdering;
    }

    public void setSearchOrdering(HistoryBusinessSearchOrdering searchOrdering) {
        this.searchOrdering = searchOrdering;
    }

    public boolean isAscSorting() {
        return ascSorting;
    }

    public void setAscSorting(boolean ascSorting) {
        this.ascSorting = ascSorting;
    }

    public LogSystemFilter getOldLogSystemFilter() {
        return oldLogSystemFilter;
    }

    public void setOldLogSystemFilter(LogSystemFilter oldLogSystemFilter) {
        this.oldLogSystemFilter = oldLogSystemFilter;
    }
}