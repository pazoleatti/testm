package com.aplana.sbrf.taxaccounting.model.util;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import org.joda.time.LocalDateTime;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Фильтр очетных периодов подразделений, null-значения соответствуют отсутствию фильтрации
 */
public class DepartmentReportPeriodFilter implements Serializable {
    private Long id;
    private Boolean isActive;
    private Boolean isCorrection;
    private LocalDateTime correctionDate;
    private List<Integer> departmentIdList;
    private List<Integer> reportPeriodIdList;
    private List<TaxType> taxTypeList;
    private Integer yearStart;
    private Integer yearEnd;
    private Integer departmentId;
    private ReportPeriod reportPeriod;
    private Date simpleCorrectionDate;
    private LocalDateTime deadline;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getYearStart() {
        return yearStart;
    }

    public void setYearStart(Integer yearStart) {
        this.yearStart = yearStart;
    }

    public Integer getYearEnd() {
        return yearEnd;
    }

    public void setYearEnd(Integer yearEnd) {
        this.yearEnd = yearEnd;
    }

    public Boolean isActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean isCorrection() {
        return isCorrection;
    }

    /**
     *
     * @param isCorrection false - если требуется найти обычные(не корректирующие) периоды
     */
    public void setIsCorrection(Boolean isCorrection) {
        this.isCorrection = isCorrection;
    }

    public LocalDateTime getCorrectionDate() {
        return correctionDate;
    }

    public void setCorrectionDate(LocalDateTime correctionDate) {
        this.correctionDate = correctionDate;
    }

    public List<Integer> getDepartmentIdList() {
        return departmentIdList;
    }

    public void setDepartmentIdList(List<Integer> departmentIdList) {
        this.departmentIdList = departmentIdList;
    }

    public List<Integer> getReportPeriodIdList() {
        return reportPeriodIdList;
    }

    public void setReportPeriodIdList(List<Integer> reportPeriodIdList) {
        this.reportPeriodIdList = reportPeriodIdList;
    }

    public List<TaxType> getTaxTypeList() {
        return taxTypeList;
    }

    public void setTaxTypeList(List<TaxType> taxTypeList) {
        this.taxTypeList = taxTypeList;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public ReportPeriod getReportPeriod() {
        return reportPeriod;
    }

    public void setReportPeriod(ReportPeriod reportPeriod) {
        this.reportPeriod = reportPeriod;
    }

    public Date getSimpleCorrectionDate() {
        return simpleCorrectionDate;
    }

    public void setSimpleCorrectionDate(Date simpleCorrectionDate) {
        this.simpleCorrectionDate = simpleCorrectionDate;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }
}
