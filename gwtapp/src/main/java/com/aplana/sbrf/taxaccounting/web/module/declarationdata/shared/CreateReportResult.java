package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.gwtplatform.dispatch.shared.Result;

public class CreateReportResult implements Result {
    private static final long serialVersionUID = 7832261980997033051L;

    private boolean existReport = false;
    private String uuid;

    public boolean isExistReport() {
        return existReport;
    }

    public void setExistReport(boolean existReport) {
        this.existReport = existReport;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
