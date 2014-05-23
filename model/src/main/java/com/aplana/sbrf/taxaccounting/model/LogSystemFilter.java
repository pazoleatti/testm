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

    private List<Long> userIds;
    private String reportPeriodName;
    private List<Long> formKind;
    private TaxType taxType;
    private Integer declarationTypeId;
    private Integer auditFormTypeId;
    private List<Long> formTypeId;
    private List<Integer> departmentIds;
    private Date fromSearchDate;
    private Date toSearchDate;

    public TaxType getTaxType() {
        return taxType;
    }

    public void setTaxType(TaxType taxType) {
        this.taxType = taxType;
    }

    /*Стартовый индекс списка записей */
    private int startIndex;

    /*Количество записей, которые нужно вернуть*/
    private int countOfRecords;

    private HistoryBusinessSearchOrdering searchOrdering;

    /*true, если сортируем по возрастанию, false - по убыванию*/
    private boolean ascSorting;

    public List<Long> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds;
    }

    public String getReportPeriodName() {
        return reportPeriodName;
    }

    public void setReportPeriodName(String reportPeriodName) {
        this.reportPeriodName = reportPeriodName;
    }

    public List<Long> getFormKind() {
        return formKind;
    }

    public void setFormKind(List<Long> formKind) {
        this.formKind = formKind;
    }

    public Integer getDeclarationTypeId() {
        return declarationTypeId;
    }

    public void setDeclarationTypeId(Integer declarationTypeId) {
        this.declarationTypeId = declarationTypeId;
    }

    public List<Long> getFormTypeId() {
        return formTypeId;
    }

    public void setFormTypeId(List<Long> formTypeId) {
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

    public Integer getAuditFormTypeId() {
        return auditFormTypeId;
    }

    public void setAuditFormTypeId(Integer auditFormTypeId) {
        this.auditFormTypeId = auditFormTypeId;
    }
}