package com.aplana.sbrf.taxaccounting.model;

import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Объект-хранилище данных для формирования специфичных отчетов декларации в скрипте
 * @author lhaziev
 */
public class ScriptSpecificDeclarationDataReportHolder {

    /**
     * Тип специфичного отчета
     */
    private DeclarationSubreport declarationSubreport;
    /**
     * Результаты формирования спец. отчета
     */
    private OutputStream fileOutputStream;
    /**
     * Файл связанный со спец. отчетом
     */
    private InputStream fileInputStream;
    /**
     * Скрипт должен вернуть название файла для сформированного отчета
     */
    private String fileName;
    /**
     * Значения параметров спец. отчета
     */
    private Map<String, Object> subreportParamValues;

    public DeclarationSubreport getDeclarationSubreport() {
        return declarationSubreport;
    }

    public void setDeclarationSubreport(DeclarationSubreport declarationSubreport) {
        this.declarationSubreport = declarationSubreport;
    }

    public OutputStream getFileOutputStream() {
        return fileOutputStream;
    }

    public void setFileOutputStream(OutputStream fileOutputStream) {
        this.fileOutputStream = fileOutputStream;
    }

    public InputStream getFileInputStream() {
        return fileInputStream;
    }

    public void setFileInputStream(InputStream fileInputStream) {
        this.fileInputStream = fileInputStream;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Map<String, Object> getSubreportParamValues() {
        return subreportParamValues;
    }

    public void setSubreportParamValues(Map<String, Object> subreportParamValues) {
        this.subreportParamValues = subreportParamValues;
    }
}
