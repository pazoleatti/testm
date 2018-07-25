package com.aplana.sbrf.taxaccounting.model.action;

import java.util.Date;

/**
 * Данные по открытию корректирующего периода
 */
public class OpenCorrectionPeriodAction {
    // основной период
    private int departmentReportPeriodId;
    // период сдачи корректировки
    private Date correctionDate;

    public OpenCorrectionPeriodAction() {
    }

    public OpenCorrectionPeriodAction(int departmentReportPeriodId, Date correctionDate) {
        this.departmentReportPeriodId = departmentReportPeriodId;
        this.correctionDate = correctionDate;
    }

    public int getDepartmentReportPeriodId() {
        return departmentReportPeriodId;
    }

    public void setDepartmentReportPeriodId(int departmentReportPeriodId) {
        this.departmentReportPeriodId = departmentReportPeriodId;
    }

    public Date getCorrectionDate() {
        return correctionDate;
    }

    public void setCorrectionDate(Date correctionDate) {
        this.correctionDate = correctionDate;
    }
}
