package com.aplana.sbrf.taxaccounting.web.module.periods.shared;

import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class CheckHasNotAcceptedFormAction extends UnsecuredActionImpl<CheckHasNotAcceptedFormResult> {
    private Integer departmentId;
    private int reportPeriodId;

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public int getReportPeriodId() {
        return reportPeriodId;
    }

    public void setReportPeriodId(int reportPeriodId) {
        this.reportPeriodId = reportPeriodId;
    }
}
