package com.aplana.sbrf.taxaccounting.model;

import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;

import java.io.OutputStream;
import java.util.List;

/**
 * Объект-хранилище данных для формирования специфичных отчетов в скрипте
 * @author lhaziev
 */
public class ScriptSpecificFormDataReportHolder {

    /**
     * Тип специфичного отчета
     */
    private String specificReportType;
    private OutputStream fileOutputStream;
    private List<DataRow<HeaderCell>> headers;
    private boolean isShowChecked;
    private boolean saved;
    /**
     * Скрипт должен вернуть название файла для сформированного отчета
     */
    private String fileName;

    public String getSpecificReportType() {
        return specificReportType;
    }

    public void setSpecificReportType(String specificReportType) {
        this.specificReportType = specificReportType;
    }

    public OutputStream getFileOutputStream() {
        return fileOutputStream;
    }

    public void setFileOutputStream(OutputStream fileOutputStream) {
        this.fileOutputStream = fileOutputStream;
    }

    public List<DataRow<HeaderCell>> getHeaders() {
        return headers;
    }

    public void setHeaders(List<DataRow<HeaderCell>> headers) {
        this.headers = headers;
    }

    public boolean isShowChecked() {
        return isShowChecked;
    }

    public void setShowChecked(boolean isShowChecked) {
        this.isShowChecked = isShowChecked;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
