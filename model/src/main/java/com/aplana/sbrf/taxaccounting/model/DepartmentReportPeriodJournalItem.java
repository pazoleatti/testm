package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.Date;

/**
 * DTO для {@link DepartmentReportPeriod}
 */

public class DepartmentReportPeriodJournalItem implements Serializable, SecuredEntity {

    private Integer id;

    private String code;

    private String name;

    private Integer year;

    private Boolean isActive;

    private Date correctionDate;

    private Integer reportPeriodId;

    private Integer departmentId;

    private Date deadline;

    private long permissions;

    private long dictTaxPeriodId;

    private Date endDate;

    /**
     * Объект для грида в окне просмотра списка периодов для группировки по году
     */
    private DepartmentReportPeriodJournalItem parent;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Date getCorrectionDate() {
        return correctionDate;
    }

    public void setCorrectionDate(Date correctionDate) {
        this.correctionDate = correctionDate;
    }

    public Integer getReportPeriodId() {
        return reportPeriodId;
    }

    public void setReportPeriodId(Integer reportPeriodId) {
        this.reportPeriodId = reportPeriodId;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public long getDictTaxPeriodId() {
        return dictTaxPeriodId;
    }

    public void setDictTaxPeriodId(long dictTaxPeriodId) {
        this.dictTaxPeriodId = dictTaxPeriodId;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public DepartmentReportPeriodJournalItem getParent() {
        return parent;
    }

    public void setParent(DepartmentReportPeriodJournalItem parent) {
        this.parent = parent;
    }

    @Override
    public long getPermissions() {
        return permissions;
    }

    @Override
    public void setPermissions(long permissions) {
        this.permissions = permissions;
    }
}
