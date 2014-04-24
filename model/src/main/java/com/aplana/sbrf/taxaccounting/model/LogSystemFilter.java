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
    private FormDataKind formKind;
    private TaxType taxType;
    private Integer declarationTypeId;
    private Integer auditFormTypeId;
    private Integer formTypeId;
    private String departmentName;
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

    public FormDataKind getFormKind() {
        return formKind;
    }

    public void setFormKind(FormDataKind formKind) {
        this.formKind = formKind;
    }

    public Integer getDeclarationTypeId() {
        return declarationTypeId;
    }

    public void setDeclarationTypeId(Integer declarationTypeId) {
        this.declarationTypeId = declarationTypeId;
    }

    public Integer getFormTypeId() {
        return formTypeId;
    }

    public void setFormTypeId(Integer formTypeId) {
        this.formTypeId = formTypeId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
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