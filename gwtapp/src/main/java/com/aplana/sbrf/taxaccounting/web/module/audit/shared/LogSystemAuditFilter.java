package com.aplana.sbrf.taxaccounting.web.module.audit.shared;

import com.aplana.sbrf.taxaccounting.model.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: avanteev
 */
public class LogSystemAuditFilter implements Serializable {

    private List<Long> auditFieldList;
    private String filter;
    private LogSystemAuditFilter oldLogSystemAuditFilter;
    private Date fromSearchDate = new Date();
    private Date toSearchDate = new Date();


    public LogSystemAuditFilter() {
        auditFieldList = new ArrayList<Long>();
    }

    public LogSystemAuditFilter(LogSystemAuditFilter filter) {
        this.fromSearchDate = filter.getFromSearchDate();
        this.toSearchDate = filter.getToSearchDate();
        this.startIndex = filter.getStartIndex();
        this.countOfRecords = filter.getCountOfRecords();
        this.searchOrdering = filter.getSearchOrdering();
        this.ascSorting = filter.isAscSorting();
        this.filter = filter.getFilter();
        this.auditFieldList = filter.getAuditFieldList();
        this.oldLogSystemAuditFilter = filter.getOldLogSystemAuditFilter();
    }

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

    public LogSystemAuditFilter getOldLogSystemAuditFilter() {
        return oldLogSystemAuditFilter;
    }

    public void setOldLogSystemAuditFilter(LogSystemAuditFilter oldLogSystemAuditFilter) {
        this.oldLogSystemAuditFilter = oldLogSystemAuditFilter;
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

    public LogSystemFilter convertTo(){
        LogSystemFilter systemFilter = new LogSystemFilter();
        systemFilter.setCountOfRecords(this.getCountOfRecords());
        systemFilter.setAscSorting(this.isAscSorting());
        systemFilter.setFromSearchDate(this.getFromSearchDate());
        systemFilter.setToSearchDate(this.getToSearchDate());
        systemFilter.setStartIndex(this.getStartIndex());
        systemFilter.setSearchOrdering(this.getSearchOrdering());
        systemFilter.setFilter(this.getFilter());
        systemFilter.setAuditFieldList(this.getAuditFieldList());

        LogSystemFilter iter = systemFilter;
        LogSystemAuditFilter iterT = this;
        while (iterT.getOldLogSystemAuditFilter() != null) {
            iterT = iterT.getOldLogSystemAuditFilter();
            LogSystemFilter oldSystemFilter = new LogSystemFilter();
            oldSystemFilter.setFromSearchDate(iterT.getFromSearchDate());
            oldSystemFilter.setToSearchDate(iterT.getToSearchDate());
            oldSystemFilter.setFilter(iterT.getFilter());
            oldSystemFilter.setAuditFieldList(iterT.getAuditFieldList());

            iter.setOldLogSystemFilter(oldSystemFilter);
            iter = oldSystemFilter;
        }
        return systemFilter;
    }

    public String toString() {
        ArrayList<String> ls = new ArrayList<String>();
        LogSystemAuditFilter logSystemAuditFilter = this;
        while (logSystemAuditFilter != null) {
            if (logSystemAuditFilter.getFilter() != null && !ls.contains(logSystemAuditFilter.getFilter())) {
                ls.add(logSystemAuditFilter.getFilter());
            }
            logSystemAuditFilter = logSystemAuditFilter.getOldLogSystemAuditFilter();
        }
        StringBuffer strBuff = new StringBuffer();
        for(int i=0; i < ls.size() ; i++) {
            strBuff.append("'").append(ls.get(i)).append("'");
            if (i != (ls.size() - 1)) strBuff.append("; ");
        }
        return strBuff.toString();
    }
}
