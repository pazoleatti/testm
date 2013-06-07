package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author ibukanov
 * Класс используется для поиска данных по журналу аудита
 */
public class LogSystemFilter implements Serializable{
	private static final long serialVersionUID = 1L;

	private int userId;
	private List<Integer> reportPeriodIds;
	private FormDataKind formKind;
	private int declarationTypeId;
	private int formTypeId;
	private List<Integer> departmentIds;
	private Date fromSearchDate;
	private Date toSearchDate;

    /*Стартовый индекс списка записей */
    private int startIndex;

    /*Количество записей, которые нужно вернуть*/
    private int countOfRecords;

    private FormDataSearchOrdering searchOrdering;

    /*true, если сортируем по возрастанию, false - по убыванию*/
    private boolean ascSorting;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public List<Integer> getReportPeriodIds() {
        return reportPeriodIds;
    }

    public void setReportPeriodIds(List<Integer> reportPeriodIds) {
        this.reportPeriodIds = reportPeriodIds;
    }

    public FormDataKind getFormKind() {
        return formKind;
    }

    public void setFormKind(FormDataKind formKind) {
        this.formKind = formKind;
    }

    public int getDeclarationTypeId() {
        return declarationTypeId;
    }

    public void setDeclarationTypeId(int declarationTypeId) {
        this.declarationTypeId = declarationTypeId;
    }

    public int getFormTypeId() {
        return formTypeId;
    }

    public void setFormTypeId(int formTypeId) {
        this.formTypeId = formTypeId;
    }

    public List<Integer> getDepartmentIds() {
        return departmentIds;
    }

    public void setDepartmentIds(List<Integer> departmentIds) {
        this.departmentIds = departmentIds;
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

    public FormDataSearchOrdering getSearchOrdering() {
        return searchOrdering;
    }

    public void setSearchOrdering(FormDataSearchOrdering searchOrdering) {
        this.searchOrdering = searchOrdering;
    }

    public boolean isAscSorting() {
        return ascSorting;
    }

    public void setAscSorting(boolean ascSorting) {
        this.ascSorting = ascSorting;
    }
}
