package com.aplana.sbrf.taxaccounting.model.action;

/**
 * Класс содержит данные передаваемые в качестве фильтра для поиска настроек подразделений
 */
public class DepartmentConfigFetchingAction {
    private Integer reportPeriodId;
    private Integer departmentId;

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
}
