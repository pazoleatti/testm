package com.aplana.sbrf.taxaccounting.web.module.periods.shared;

import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class CheckHasManualEditFormAction extends UnsecuredActionImpl<CheckHasManualEditFormResult> {
    private Integer departmentId;
    private int reportPeriodId;
    private TaxType taxType;
    private FormDataKind kind;

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

    public TaxType getTaxType() {
        return taxType;
    }

    public void setTaxType(TaxType taxType) {
        this.taxType = taxType;
    }

    public FormDataKind getKind() {
        return kind;
    }

    public void setKind(FormDataKind kind) {
        this.kind = kind;
    }
}
