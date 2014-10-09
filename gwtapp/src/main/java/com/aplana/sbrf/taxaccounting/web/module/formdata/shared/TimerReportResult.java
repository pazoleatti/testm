package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.gwtplatform.dispatch.shared.Result;

public class TimerReportResult implements Result {
    private static final long serialVersionUID = 7832261980997033051L;

    public static enum StatusReport {
        EXIST, //существует
        LOCKED, //есть блокировка
        NOT_EXIST //не существует
    }
    private StatusReport existReport;

    public StatusReport getExistReport() {
        return existReport;
    }

    public void setExistReport(StatusReport existReport) {
        this.existReport = existReport;
    }
}
