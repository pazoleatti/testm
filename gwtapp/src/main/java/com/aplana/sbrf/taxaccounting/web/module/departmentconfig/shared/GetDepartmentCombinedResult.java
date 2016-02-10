package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared;

import com.gwtplatform.dispatch.shared.Result;

import java.util.Map;

/**
 * @author Dmitriy Levykin
 */
public class GetDepartmentCombinedResult implements Result {

    // Параметры подразделения
    private DepartmentCombined departmentCombined;

    // Текстовые значения справочников
    private Map<Long, String> rbTextValues;

    // Признак открытости выбранного отчетного периода
    private boolean isReportPeriodActive;

    private String uuid;
    private String errorMsg;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public DepartmentCombined getDepartmentCombined() {
        return departmentCombined;
    }

    public void setDepartmentCombined(DepartmentCombined departmentCombined) {
        this.departmentCombined = departmentCombined;
    }

    public Map<Long, String> getRbTextValues() {
        return rbTextValues;
    }

    public void setRbTextValues(Map<Long, String> rbTextValues) {
        this.rbTextValues = rbTextValues;
    }

    public boolean isReportPeriodActive() {
        return isReportPeriodActive;
    }

    public void setReportPeriodActive(boolean reportPeriodActive) {
        isReportPeriodActive = reportPeriodActive;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}
