package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.aplana.sbrf.taxaccounting.web.widget.pdfviewer.shared.Pdf;
import com.gwtplatform.dispatch.shared.Result;

import java.util.Map;

public class TimerSubreportResult implements Result {
    private static final long serialVersionUID = 1651645964813132154L;

    public enum StatusReport {
        EXIST, //существует
        LOCKED, //есть блокировка
        NOT_EXIST, //не существует
        LIMIT // превышен лимит требований по формированию отчета
    }
    private Map<String, StatusReport> mapExistReport;

    public Map<String, StatusReport> getMapExistReport() {
        return mapExistReport;
    }

    public void setMapExistReport(Map<String, StatusReport> mapExistReport) {
        this.mapExistReport = mapExistReport;
    }
}
