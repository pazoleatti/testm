package com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class GetReportPeriodsAction extends UnsecuredActionImpl<GetReportPeriodsResult> {

	private TaxType taxType;
    private Integer reportPeriodId;
    private Integer departmentId;
    private boolean isDownloadReports;

    public TaxType getTaxType() {
        return taxType;
    }

    public void setTaxType(TaxType taxType) {
        this.taxType = taxType;
    }

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

    public boolean isDownloadReports() {
        return isDownloadReports;
    }

    public void setDownloadReports(boolean isDownloadReports) {
        this.isDownloadReports = isDownloadReports;
    }
}
