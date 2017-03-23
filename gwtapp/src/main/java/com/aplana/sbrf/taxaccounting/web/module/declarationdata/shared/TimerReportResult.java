package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;

public class TimerReportResult extends DeclarationDataResult {
    private static final long serialVersionUID = 7859961980147513071L;

    public static final Status STATUS_LOCKED = new Status(StatusReport.LOCKED);
    public static final Status STATUS_NOT_EXIST = new Status(StatusReport.NOT_EXIST);
    public static final Status STATUS_LIMIT = new Status(StatusReport.LIMIT);

    public enum StatusReport {
        EXIST, //существует
        LOCKED, //есть блокировка
        NOT_EXIST, //не существует
        LIMIT // превышен лимит требований по формированию отчета
    }

    public static class Status implements IsSerializable, Serializable {
        private StatusReport statusReport;
        private String createDate;

        public Status() {
            this.statusReport = null;
            this.createDate = "";
        }

        public Status(StatusReport statusReport) {
            this.statusReport = statusReport;
            this.createDate = "";
        }

        public Status(StatusReport statusReport, String createDate) {
            this.statusReport = statusReport;
            this.createDate = createDate;
        }

        public String getCreateDate() {
            return createDate;
        }

        public StatusReport getStatusReport() {
            return statusReport;
        }
    }

    private Status existReport;
    private Status existXMLReport; // только для PDF отчетов, отображается статус формированяи PDF


    public Status getExistReport() {
        return existReport;
    }

    public void setExistReport(Status existReport) {
        this.existReport = existReport;
    }

    public Status getExistXMLReport() {
        return existXMLReport;
    }

    public void setExistXMLReport(Status existXMLReport) {
        this.existXMLReport = existXMLReport;
    }
}
