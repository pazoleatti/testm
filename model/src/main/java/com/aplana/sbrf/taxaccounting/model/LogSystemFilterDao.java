package com.aplana.sbrf.taxaccounting.model;

import java.util.Date;
import java.util.List;

/**
 * User: avanteev
 */
public class LogSystemFilterDao {
    private List<Long> userIds;
    private Date fromSearchDate;
    private Date toSearchDate;
    private String departmentName;
    private String reportPeriodName;
    private int countOfRecords;
    private HistoryBusinessSearchOrdering searchOrdering;
    /*true, если сортируем по возрастанию, false - по убыванию*/
    private boolean ascSorting;
    /*Стартовый индекс списка записей */
    private int startIndex;

    private List<Integer> reportPeriodIds;

    private List<Integer> departmentIds;

    private List<Long> formTypeIds;

    private List<FormDataKind> formDataKinds;

    private List<TaxType> taxTypes;

    private Integer auditFormTypeId;
    private Integer declarationTypeId;

    public Integer getDeclarationTypeId() {
        return declarationTypeId;
    }

    public void setDeclarationTypeId(Integer declarationTypeId) {
        this.declarationTypeId = declarationTypeId;
    }

    public Integer getAuditFormTypeId() {
        return auditFormTypeId;
    }

    public void setAuditFormTypeId(Integer auditFormTypeId) {
        this.auditFormTypeId = auditFormTypeId;
    }

    public List<Integer> getReportPeriodIds() {
        return reportPeriodIds;
    }

    public void setReportPeriodIds(List<Integer> reportPeriodIds) {
        this.reportPeriodIds = reportPeriodIds;
    }

    public List<Integer> getDepartmentIds() {
        return departmentIds;
    }

    public void setDepartmentIds(List<Integer> departmentIds) {
        this.departmentIds = departmentIds;
    }

    public List<Long> getFormTypeIds() {
        return formTypeIds;
    }

    public void setFormTypeIds(List<Long> formTypeIds) {
        this.formTypeIds = formTypeIds;
    }

    public List<FormDataKind> getFormDataKinds() {
        return formDataKinds;
    }

    public void setFormDataKinds(List<FormDataKind> formDataKinds) {
        this.formDataKinds = formDataKinds;
    }

    public List<TaxType> getTaxTypes() {
        return taxTypes;
    }

    public void setTaxTypes(List<TaxType> taxTypes) {
        this.taxTypes = taxTypes;
    }

    public List<Long> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds;
    }

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

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }
}
