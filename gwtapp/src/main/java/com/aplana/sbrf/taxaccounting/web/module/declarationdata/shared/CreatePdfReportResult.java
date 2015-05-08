package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.gwtplatform.dispatch.shared.Result;

public class CreatePdfReportResult implements Result {
    private static final long serialVersionUID = 7832261980997033051L;

    private boolean existReport = false;
    private boolean existTask = false;

    private String uuid;

    public boolean isExistReport() {
        return existReport;
    }

    public void setExistReport(boolean existReport) {
        this.existReport = existReport;
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
