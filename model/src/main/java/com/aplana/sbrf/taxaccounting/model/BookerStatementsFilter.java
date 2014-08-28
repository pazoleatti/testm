package com.aplana.sbrf.taxaccounting.model;

import com.aplana.sbrf.taxaccounting.model.util.StringUtils;

import java.io.Serializable;
import java.util.List;

/**
 * @author lhaziev
 */
public class BookerStatementsFilter implements Serializable {
    private static final long serialVersionUID = -756162324197L;

    private List<Integer> accountPeriodIds;
    private List<Integer> departmentIds;
    private BookerStatementsType bookerStatementsType;

    /*Стартовый индекс списка записей */
    private int startIndex;

    /*Количество записей, которые нужно вернуть*/
    private int countOfRecords;

    private BookerStatementsSearchOrdering searchOrdering;

    /*true, если сортируем по возрастанию, false - по убыванию*/
    private boolean ascSorting;

    public List<Integer> getAccountPeriodIds() {
        return accountPeriodIds;
    }

    public void setAccountPeriodIds(List<Integer> accountPeriodIds) {
        this.accountPeriodIds = accountPeriodIds;
    }

    public List<Integer> getDepartmentIds() {
        return departmentIds;
    }

    public void setDepartmentIds(List<Integer> departmentIds) {
        this.departmentIds = departmentIds;
    }

    public BookerStatementsType getBookerStatementsType() {
        return bookerStatementsType;
    }

    public void setBookerStatementsType(BookerStatementsType bookerStatementsType) {
        this.bookerStatementsType = bookerStatementsType;
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

    public BookerStatementsSearchOrdering getSearchOrdering() {
        return searchOrdering;
    }

    public void setSearchOrdering(BookerStatementsSearchOrdering searchOrdering) {
        this.searchOrdering = searchOrdering;
    }

    public boolean isAscSorting() {
        return ascSorting;
    }

    public void setAscSorting(boolean ascSorting) {
        this.ascSorting = ascSorting;
    }

    @Override
    public String toString() {
        return "[accountPeriodIds = "+ (accountPeriodIds != null ? "[" + (accountPeriodIds.size()> 0 ? StringUtils.join(accountPeriodIds.toArray(), ',') : "") + "]" : "null") +
                "; departmentIds = "+ (departmentIds != null ? "[" + (departmentIds.size()> 0 ? StringUtils.join(departmentIds.toArray(), ',') : "") + "]" : "null") +
                "; bookerStatementsType = " + bookerStatementsType +
                "; startIndex = " + startIndex +
                "; countOfRecords = " + countOfRecords +
                "; searchOrdering = " + searchOrdering +
                "; ascSorting = " + ascSorting + "]";
    }
}
