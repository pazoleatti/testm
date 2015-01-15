package com.aplana.sbrf.taxaccounting.model.source;

/**
 * Данные принятого источника
 * @author Lhaziev
 */
public class AcceptedFormData {

    private String periodInfo;
    private int reportPeriodId;
    private int formTypeId;

    public String getPeriodInfo() {
        return periodInfo;
    }

    public void setPeriodInfo(String periodInfo) {
        this.periodInfo = periodInfo;
    }

    public int getReportPeriodId() {
        return reportPeriodId;
    }

    public void setReportPeriodId(int reportPeriodId) {
        this.reportPeriodId = reportPeriodId;
    }

    public int getFormTypeId() {
        return formTypeId;
    }

    public void setFormTypeId(int formTypeId) {
        this.formTypeId = formTypeId;
    }
}
