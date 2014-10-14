package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.shared.Pdf;
import com.gwtplatform.dispatch.shared.Result;

public class TimerReportResult implements Result {
    private static final long serialVersionUID = 7859961980147513071L;

    public static enum StatusReport {
        EXIST, //существует
        LOCKED, //есть блокировка
        NOT_EXIST //не существует
    }
    private StatusReport existReport;
    private Pdf pdf;

    public StatusReport getExistReport() {
        return existReport;
    }

    public void setExistReport(StatusReport existReport) {
        this.existReport = existReport;
    }

    public Pdf getPdf() {
        return pdf;
    }

    public void setPdf(Pdf pdf) {
        this.pdf = pdf;
    }
}
