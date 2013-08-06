package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.gwtplatform.dispatch.shared.Result;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Dmitriy Levykin
 */
public class GetDepartmentCombinedResult implements Result {

    // Параметры подразделения
    private DepartmentCombined departmentCombined;
    // Доступные отчетные периоды
    private List<ReportPeriod> periods;
    // Текстовые значения справочников
    private Map<Long, String> rbTextValues;

    public DepartmentCombined getDepartmentCombined() {
        return departmentCombined;
    }

    public void setDepartmentCombined(DepartmentCombined departmentCombined) {
        this.departmentCombined = departmentCombined;
    }

    public List<ReportPeriod> getPeriods() {
        return periods;
    }

    public void setPeriods(List<ReportPeriod> periods) {
        this.periods = periods;
    }

    public Map<Long, String> getRbTextValues() {
        return rbTextValues;
    }

    public void setRbTextValues(Map<Long, String> rbTextValues) {
        this.rbTextValues = rbTextValues;
    }
}
