package com.aplana.sbrf.taxaccounting.web.module.audit.shared;

import com.aplana.sbrf.taxaccounting.model.*;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * User: avanteev
 */
public class LogSystemAuditFilter implements Serializable {

    private List<Long> userIds;
    private String reportPeriodName;
    private List<Long> formKind;
    private TaxType taxType;
    private Integer declarationTypeId;
    private AuditFormType auditFormTypeId;
    private List<Long> formTypeIds;
    private String departmentName;
    private Date fromSearchDate;
    private Date toSearchDate;


    public LogSystemAuditFilter() {
    }

    public LogSystemAuditFilter(LogSystemAuditFilter filter) {
        this.userIds = filter.getUserIds();
        this.reportPeriodName = filter.getReportPeriodName();
        this.formKind = filter.getFormKind();
        this.taxType = filter.getTaxType();
        this.declarationTypeId = filter.getDeclarationTypeId();
        this.auditFormTypeId = filter.getAuditFormTypeId();
        this.formTypeIds = filter.getFormTypeIds();
        this.departmentName = filter.getDepartmentName();
        this.fromSearchDate = filter.getFromSearchDate();
        this.toSearchDate = filter.getToSearchDate();
        this.startIndex = filter.getStartIndex();
        this.countOfRecords = filter.getCountOfRecords();
        this.searchOrdering = filter.getSearchOrdering();
        this.ascSorting = filter.isAscSorting();
    }

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

    public List<Long> getFormTypeIds() {
        return formTypeIds;
    }

    public void setFormTypeIds(List<Long> formTypeIds) {
        this.formTypeIds = formTypeIds;
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

    public AuditFormType getAuditFormTypeId() {
        return auditFormTypeId;
    }

    public void setAuditFormTypeId(AuditFormType auditFormTypeId) {
        this.auditFormTypeId = auditFormTypeId;
    }

    public LogSystemFilter convertTo(){
        LogSystemFilter systemFilter = new LogSystemFilter();
        systemFilter.setCountOfRecords(this.getCountOfRecords());
		systemFilter.setFormKind(formKind);
		systemFilter.setDepartmentName(this.departmentName);
        systemFilter.setTaxType(this.getTaxType());
        systemFilter.setAscSorting(this.isAscSorting());
        systemFilter.setAuditFormTypeId(auditFormTypeId != null? auditFormTypeId.getId() : null);
        systemFilter.setDeclarationTypeId(this.getDeclarationTypeId());
        systemFilter.setFormTypeId(formTypeIds);
        systemFilter.setFromSearchDate(this.getFromSearchDate());
        systemFilter.setToSearchDate(this.getToSearchDate());
        systemFilter.setReportPeriodName(this.getReportPeriodName());
        systemFilter.setUserIds(this.getUserIds());
        systemFilter.setStartIndex(this.getStartIndex());
        systemFilter.setSearchOrdering(this.getSearchOrdering());

        return systemFilter;
    }
}
