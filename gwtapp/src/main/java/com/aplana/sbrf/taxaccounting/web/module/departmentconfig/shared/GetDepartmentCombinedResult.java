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
}
