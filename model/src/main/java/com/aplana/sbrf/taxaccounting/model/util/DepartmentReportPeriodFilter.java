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
    private Boolean isBalance;
    private Boolean isCorrection;
    private Date correctionDate;
    private List<Integer> departmentIdList;
    private List<Integer> reportPeriodIdList;
    private List<TaxType> taxTypeList;

    public Boolean isActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean isBalance() {
        return isBalance;
    }

    public void setIsBalance(Boolean isBalance) {
        this.isBalance = isBalance;
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
