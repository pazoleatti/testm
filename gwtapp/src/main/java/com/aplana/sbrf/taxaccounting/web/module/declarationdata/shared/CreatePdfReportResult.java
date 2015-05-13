package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.gwtplatform.dispatch.shared.Result;

public class CreatePdfReportResult implements Result {
    private static final long serialVersionUID = 7832261980997033051L;

    private boolean existReportXml = false;
    private boolean existTask = false;

    private String uuid;

    public boolean isExistReportXml() {
        return existReportXml;
    }

    public void setExistReportXml(boolean existReportXml) {
        this.existReportXml = existReportXml;
    }

    public boolean isExistTask() {
        return existTask;
    }

    public void setExistTask(boolean existTask) {
        this.existTask = existTask;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
