package com.aplana.sbrf.taxaccounting.web.module.periods.shared;

import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.Date;

public class CheckHasNotAcceptedFormAction extends UnsecuredActionImpl<CheckHasNotAcceptedFormResult> {
    private Integer departmentId;
    private int reportPeriodId;
    private Date correctPeriod;

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

    public Date getCorrectPeriod() {
        return correctPeriod;
    }

    public void setCorrectPeriod(Date correctPeriod) {
        this.correctPeriod = correctPeriod;
    }
}
