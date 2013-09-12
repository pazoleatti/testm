package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 *   Action формы настроек подразделений
 *   @author Dmitriy Levykin
 */
public class SaveDepartmentCombinedAction extends UnsecuredActionImpl<SaveDepartmentCombinedResult> implements ActionName {

    private DepartmentCombined departmentCombined;
    private Integer period;

    public DepartmentCombined getDepartmentCombined() {
        return departmentCombined;
    }

    public void setDepartmentCombined(DepartmentCombined departmentCombined) {
        this.departmentCombined = departmentCombined;
    }

    public Integer getReportPeriodId() {
        return period;
    }

    public void setReportPeriodId(Integer period) {
        this.period = period;
    }

    @Override
    public String getName() {
        return "Сохранение деталей подразделения";
    }
}
