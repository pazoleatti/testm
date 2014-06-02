package com.aplana.sbrf.taxaccounting.web.module.periods.shared;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.Date;

public class EditPeriodAction extends UnsecuredActionImpl<EditPeriodResult> {
    int year;
    int reportPeriodId;
    int newReportPeriodId;
    int newDictTaxPeriodId;
    TaxType taxType;
    long departmentId;
    Date correctionDate;
    Date newCorrectionDate;
    boolean isBalance;

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public TaxType getTaxType() {
        return taxType;
    }

    public void setTaxType(TaxType taxType) {
        this.taxType = taxType;
    }

    public long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(long departmentId) {
        this.departmentId = departmentId;
    }

    public Date getCorrectionDate() {
        return correctionDate;
    }

    public void setCorrectionDate(Date correctionDate) {
        this.correctionDate = correctionDate;
    }

    public Date getNewCorrectionDate() {
        return newCorrectionDate;
    }

    public void setNewCorrectionDate(Date newCorrectionDate) {
        this.newCorrectionDate = newCorrectionDate;
    }

    public boolean isBalance() {
        return isBalance;
    }

    public void setBalance(boolean isBalance) {
        this.isBalance = isBalance;
    }

    public int getReportPeriodId() {
        return reportPeriodId;
    }

    public void setReportPeriodId(int reportPeriodId) {
        this.reportPeriodId = reportPeriodId;
    }

    public int getNewReportPeriodId() {
        return newReportPeriodId;
    }

    public void setNewReportPeriodId(int newReportPeriodId) {
        this.newReportPeriodId = newReportPeriodId;
    }

    public int getNewDictTaxPeriodId() {
        return newDictTaxPeriodId;
    }

    public void setNewDictTaxPeriodId(int newDictTaxPeriodId) {
        this.newDictTaxPeriodId = newDictTaxPeriodId;
    }
}
