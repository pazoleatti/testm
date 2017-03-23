package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.aplana.sbrf.taxaccounting.model.PrepareSpecificReportResult;

public class PrepareSubreportResult extends DeclarationDataResult {
    private static final long serialVersionUID = 1913464641303440641L;

    private PrepareSpecificReportResult prepareSpecificReportResult;
    private String uuid;

    public PrepareSpecificReportResult getPrepareSpecificReportResult() {
        return prepareSpecificReportResult;
    }

    public void setPrepareSpecificReportResult(PrepareSpecificReportResult prepareSpecificReportResult) {
        this.prepareSpecificReportResult = prepareSpecificReportResult;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
