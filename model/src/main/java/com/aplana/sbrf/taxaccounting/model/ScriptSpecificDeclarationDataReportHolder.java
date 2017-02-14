package com.aplana.sbrf.taxaccounting.model;

import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Объект-хранилище данных для формирования специфичных отчетов декларации в скрипте
 *
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

    private PrepareSpecificReportResult prepareSpecificReportResult;

    private DataRow<Cell> selectedRecord;

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

    public Object getSubreportParamNullCheck(String alias) {
        Object result = getSubreportParam(alias);
        if (result != null) {
            return result;
        } else {
            throw new ServiceException("Не указано значение обязательного параметра: '"+alias+"'!");
        }
    }

    public Object getSubreportParam(String alias) {
        if (subreportParamValues != null) {
            return subreportParamValues.get(alias);
        }
        return null;
    }

    public PrepareSpecificReportResult getPrepareSpecificReportResult() {
        return prepareSpecificReportResult;
    }

    public void setPrepareSpecificReportResult(PrepareSpecificReportResult prepareSpecificReportResult) {
        this.prepareSpecificReportResult = prepareSpecificReportResult;
    }

    public DataRow<Cell> getSelectedRecord() {
        return selectedRecord;
    }

    public void setSelectedRecord(DataRow<Cell> selectedRecord) {
        this.selectedRecord = selectedRecord;
    }
}
