package com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class GetDeclarationDepartmentsAction extends UnsecuredActionImpl<GetDeclarationDepartmentsResult> {
    private TaxType taxType;
    private int reportPeriodId;
    private boolean isReports;

    public TaxType getTaxType() {
        return taxType;
    }

    public void setTaxType(TaxType taxType) {
        this.taxType = taxType;
    }

    public int getReportPeriodId() {
        return reportPeriodId;
    }

    public void setReportPeriodId(int reportPeriodId) {
        this.reportPeriodId = reportPeriodId;
    }

    public boolean isReports() {
        return isReports;
    }

    public void setReports(boolean isReports) {
        this.isReports = isReports;
    }
}
