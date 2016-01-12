package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.shared.Pdf;
import com.gwtplatform.dispatch.shared.Result;

public class TimerReportResult implements Result {
    private static final long serialVersionUID = 7859961980147513071L;

    public enum StatusReport {
        EXIST, //существует
        LOCKED, //есть блокировка
        NOT_EXIST, //не существует
        LIMIT // превышен лимит требований по формированию отчета
    }
    private StatusReport existReport;
    private Pdf pdf;
    private StatusReport existXMLReport; // только для PDF отчетов, отображается статус формированяи PDF


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

    public StatusReport getExistXMLReport() {
        return existXMLReport;
    }

    public void setExistXMLReport(StatusReport existXMLReport) {
        this.existXMLReport = existXMLReport;
    }
}
