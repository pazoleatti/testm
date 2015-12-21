package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.gwtplatform.dispatch.shared.Result;

import java.util.Map;

public class TimerReportResult implements Result {
    private static final long serialVersionUID = 1565465465645639119L;

    public static enum StatusReport {
        EXIST, //существует
        LOCKED, //есть блокировка
        NOT_EXIST //не существует
    }
    private Map<String, StatusReport> mapExistReport;

    public Map<String, StatusReport> getMapExistReport() {
        return mapExistReport;
    }

    public void setMapExistReport(Map<String, StatusReport> mapExistReport) {
        this.mapExistReport = mapExistReport;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }
}
