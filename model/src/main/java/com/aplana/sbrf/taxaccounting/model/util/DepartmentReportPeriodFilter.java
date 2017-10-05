package com.aplana.sbrf.taxaccounting.model.util;

import com.aplana.sbrf.taxaccounting.model.TaxType;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Фильтр очетных периодов подразделений, null-значения соответствуют отсутствию фильтрации
 */
public class DepartmentReportPeriodFilter implements Serializable {
    private Boolean isActive;
    private Boolean isCorrection;
    private Date correctionDate;
    private List<Integer> departmentIdList;
    private List<Integer> reportPeriodIdList;
    private List<TaxType> taxTypeList;
    private Integer yearStart;
    private Integer yearEnd;

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

    public Date getCorrectionDate() {
        return correctionDate;
    }

    public void setCorrectionDate(Date correctionDate) {
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
}
