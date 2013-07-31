package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.gwtplatform.dispatch.shared.Result;

import java.util.HashMap;
import java.util.List;

/**
 * @author Dmitriy Levykin
 */
public class GetDepartmentCombinedResult implements Result {

    private DepartmentCombined departmentCombined;
    private List<ReportPeriod> periods;

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
}
