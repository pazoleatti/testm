package com.aplana.sbrf.taxaccounting.model.result;

import com.aplana.sbrf.taxaccounting.model.PrepareSpecificReportResult;

public class PrepareSubreportResult extends DeclarationDataResult {

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
