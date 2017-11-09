package com.aplana.sbrf.taxaccounting.model;


import org.joda.time.LocalDateTime;

import java.io.Serializable;

/**
 * DTO для {@link DepartmentReportPeriod}
 */

public class DepartmentReportPeriodJournalItem implements Serializable, SecuredEntity{

    private Long id;

    private String name;

    private Integer year;

    private Byte isActive;

    private LocalDateTime correctionDate;

    private LocalDateTime deadline;

    private long permissions;

    private long dictTaxPeriodId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Byte getIsActive() {
        return isActive;
    }

    public void setIsActive(Byte isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCorrectionDate() {
        return correctionDate;
    }

    public void setCorrectionDate(LocalDateTime correctionDate) {
        this.correctionDate = correctionDate;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public long getDictTaxPeriodId() {
        return dictTaxPeriodId;
    }

    public void setDictTaxPeriodId(long dictTaxPeriodId) {
        this.dictTaxPeriodId = dictTaxPeriodId;
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
