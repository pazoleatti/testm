package com.aplana.sbrf.taxaccounting.model;

import java.util.Date;
import java.util.List;

/**
 * User: avanteev
 */
public class LogSystemFilterDao {
    List<Long> formDataIds;
    List<Long> declarationDataIds;
    private Date fromSearchDate;
    private Date toSearchDate;
    private String reportPeriodName;
    private int countOfRecords;
    private HistoryBusinessSearchOrdering searchOrdering;
    /*true, если сортируем по возрастанию, false - по убыванию*/
    private boolean ascSorting;
    /*Стартовый индекс списка записей */
    private int startIndex;

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
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

    public String getReportPeriodName() {
        return reportPeriodName;
    }

    public void setReportPeriodName(String reportPeriodName) {
        this.reportPeriodName = reportPeriodName;
    }

    public int getCountOfRecords() {
        return countOfRecords;
    }

    public void setCountOfRecords(int countOfRecords) {
        this.countOfRecords = countOfRecords;
    }

    public List<Long> getFormDataIds() {
        return formDataIds;
    }

    public void setFormDataIds(List<Long> formDataIds) {
        this.formDataIds = formDataIds;
    }

    public List<Long> getDeclarationDataIds() {
        return declarationDataIds;
    }

    public void setDeclarationDataIds(List<Long> declarationDataIds) {
        this.declarationDataIds = declarationDataIds;
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
}
